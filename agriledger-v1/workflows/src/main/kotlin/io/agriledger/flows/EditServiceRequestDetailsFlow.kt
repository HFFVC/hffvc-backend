package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.BatchContract
import io.agriledger.contracts.SalesContract
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.*
import io.agriledger.model.api.EditServiceRequestDTO
import io.agriledger.model.enumerations.Product
import io.agriledger.states.SalesState
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception
import java.time.LocalDateTime

object EditServiceRequestDetailsFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: EditServiceRequestDTO) : FlowLogic<SignedTransaction>() {
        companion object {
            object EXTRACTING_VAULT_STATES : ProgressTracker.Step("Extracting states from the vault.")
            object TX_COMPONENTS : ProgressTracker.Step("Gathering a transaction's components.")
            object TX_BUILDING : ProgressTracker.Step("Building a transaction.")
            object TX_SIGNING : ProgressTracker.Step("Signing a transaction.")
            object TX_VERIFICATION : ProgressTracker.Step("Verifying a transaction.")
            object FINALISATION : ProgressTracker.Step("Finalising a transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    EXTRACTING_VAULT_STATES,
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
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)

            // We create the transaction components.

            progressTracker.currentStep = EXTRACTING_VAULT_STATES

            val wantedStateRef = QueryVault().queryServiceRequestById(reqParam.id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Service request ${reqParam.id} not found.")

            val vaultData = wantedStateRef.state.data

            val outputState: ServiceRequestState = vaultData.copy(
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
                    )
            )

            progressTracker.currentStep = TX_COMPONENTS

            val command = Command(ServiceRequestContract.Commands.EditServiceRequestDetails(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
                    .addInputState(wantedStateRef)
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

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val otherParties = listOf<Party>(lsp, sae, collector, broker).filter { it.name != ourIdentity.name }
            val partySessions = mutableListOf<FlowSession>()
            otherParties.forEach {
                val session = initiateFlow(it)
                partySessions.add(session)
            }

            return subFlow(FinalityFlow(signedTx, partySessions))
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