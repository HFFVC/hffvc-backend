package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.ServiceRequestCreationModel
import io.agriledger.model.api.ServiceRequestCreationDTO
import io.agriledger.model.enumerations.Product
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.cordapp.CordappConfigException
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception
import java.time.LocalDateTime


object CreateServiceRequestFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            val reqParam: ServiceRequestCreationDTO, val email: String?
    ) : FlowLogic<SignedTransaction>() {
        companion object {
            object TX_COMPONENTS : ProgressTracker.Step("Gathering a transaction's components.")
            object TX_BUILDING : ProgressTracker.Step("Building a transaction.")
            object TX_SIGNING : ProgressTracker.Step("Signing a transaction.")
            object TX_VERIFICATION : ProgressTracker.Step("Verifying a transaction.")
            object FINALISATION : ProgressTracker.Step("Finalising a transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    TX_COMPONENTS,
                    TX_BUILDING,
                    TX_SIGNING,
                    TX_VERIFICATION,
                    FINALISATION
            )
        }

        override val progressTracker: ProgressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            if (!validateFlowConstraints(reqParam)) {
                throw FlowException("Could not initiate the transaction. One or more flow constraints were not met.")
            }
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)


            // We create the transaction components.
            progressTracker.currentStep = TX_COMPONENTS

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val existingServiceRequest =
                    QueryVault().queryServiceRequestById(reqParam.id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            if (existingServiceRequest != null) {
                throw Exception("service request ${reqParam.id} already exists.")
            }

            val outputState = ServiceRequestState(
                    id = reqParam.id,
                    creationData = ServiceRequestCreationModel(
                            department = reqParam.department,
                            estimatedNoFruits = reqParam.estimatedNoFruits.toInt(),
                            farmerId = reqParam.farmerId,
                            producer = reqParam.producer,
                            product = Product.valueOf(reqParam.product.toUpperCase()),
                            requestedOn = LocalDateTime.parse(reqParam.requestedOn),
                            status = reqParam.status,
                            town = reqParam.town,
                            location = reqParam.location,
                            displayId = reqParam.displayId,
                            phoneNo = reqParam.phoneNo,
                            cin = reqParam.cin,
                            nif = reqParam.nif
                    ),
                    sae = ourIdentity,
                    broker = broker,
                    collector = collector,
                    lsp = lsp,
                    signatories = mutableListOf(ourIdentity),
                    usernames = mutableSetOf(email!!)
            )
            val command = Command(ServiceRequestContract.Commands.Create(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(outputState, ServiceRequestContract.ID)
                    .addCommand(command)


            // Signing the transaction.
            progressTracker.currentStep = TX_SIGNING
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Verifying the transaction.
            progressTracker.currentStep = TX_VERIFICATION
            txBuilder.verify(serviceHub)

            // We finalise the transaction and then send it to the counterparty.
            progressTracker.currentStep = FINALISATION

            val lspSession = initiateFlow(lsp)
            val collectorSession = initiateFlow(collector)
            val brokerSession = initiateFlow(broker)

            return subFlow(FinalityFlow(signedTx, listOf(lspSession, collectorSession, brokerSession)))

        }

        private fun validateFlowConstraints(reqParam: ServiceRequestCreationDTO): Boolean {
            if (
                    reqParam.farmerId.isBlank() ||
                    reqParam.department.isBlank() ||
                    reqParam.estimatedNoFruits.isBlank() ||
                    reqParam.estimatedNoFruits.toInt() <= 0 ||
                    reqParam.producer.isBlank() ||
                    reqParam.location.isBlank() ||
                    reqParam.displayId.isBlank() ||
                    reqParam.product.isBlank() ||
                    reqParam.requestedOn.isBlank()
            ) {
                return false
            }
            return true
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            // Responder flow logic goes here.
            subFlow(ReceiveFinalityFlow(counterpartySession))
        }
    }
}