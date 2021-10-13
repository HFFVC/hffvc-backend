package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.BatchContract
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.api.BatchDTO
import io.agriledger.states.BatchState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.LocalDateTime

object CreateBatchFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: BatchDTO, val email: String?) : FlowLogic<SignedTransaction>() {

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
            if (!validateFlowConstraints(reqParam)) {
                throw FlowException("Could not initiate the transaction. One or more flow constraints were not met.")
            }
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)

            // We create the transaction components.

            progressTracker.currentStep = EXTRACTING_VAULT_STATES

            // checking if Batch ID already exists
            val batchIdExists = QueryVault().queryBatchById(reqParam.batchId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            if (batchIdExists != null) {
                throw Exception("Batch Id ${reqParam.batchId} already exists.")
            }
            val allWantedStateRefs = reqParam.lotIds.map {
                QueryVault().queryServiceRequestById(it, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            }

            if (allWantedStateRefs.size == 0) {
                throw Exception("Lot ID not Found")
            }
            val allWantedStates = allWantedStateRefs.map { it?.state?.data }

            if (allWantedStates.first() != null) {
                if (allWantedStates.first()!!.lsp != ourIdentity) {
                    throw IllegalArgumentException("Could not proceed with flow execution. The party is not authorised to run this flow.")
                }
            }

            val allServiceRequestOutputStates = allWantedStates.map {
                it?.copy(
                        batchId = reqParam.batchId
                )
            }

            progressTracker.currentStep = TX_COMPONENTS

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")


            val batchOutputState = BatchState(
                    batchId = reqParam.batchId,
                    batchCreatedAt = LocalDateTime.parse(reqParam.batchCreatedAt),
                    lsp = ourIdentity,
                    sae = sae,
                    collector = collector,
                    broker = broker,
                    palletStartQRCode = reqParam.palletStartQRCode,
                    palletEndQRCode = reqParam.palletEndQRCode,
                    signatories = mutableListOf(ourIdentity),
                    usernames = mutableSetOf(email!!)
            )

            val serviceRequestCommand = Command(ServiceRequestContract.Commands.BatchAssociation(), listOf(ourIdentity.owningKey))
            val batchCommand = Command(BatchContract.BatchCommands.CreateBatch(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)

            allWantedStateRefs.map {
                if (it != null) {
                    txBuilder.addInputState(it)
                }
            }
            allServiceRequestOutputStates.map {
                if (it != null) {
                    txBuilder.addOutputState(it, ServiceRequestContract.ID)
                }
            }
            txBuilder.addOutputState(batchOutputState, BatchContract.ID)
            txBuilder.addCommand(serviceRequestCommand)
            txBuilder.addCommand(batchCommand)

            // Signing the transaction.
            progressTracker.currentStep = TX_SIGNING
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Verifying the transaction.
            progressTracker.currentStep = TX_VERIFICATION
            txBuilder.verify(serviceHub)

            // We finalise the transaction and then send it to the counterparty.
            progressTracker.currentStep = FINALISATION

            val saeSession = initiateFlow(sae)
            val collectorSession = initiateFlow(collector)
            val brokerSession = initiateFlow(broker)

            return subFlow(FinalityFlow(signedTx, listOf(saeSession, collectorSession, brokerSession)))
        }

        private fun validateFlowConstraints(reqParam: BatchDTO): Boolean {
            if (
                    reqParam.batchCreatedAt.isBlank() ||
                    reqParam.lotIds.size == 0
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