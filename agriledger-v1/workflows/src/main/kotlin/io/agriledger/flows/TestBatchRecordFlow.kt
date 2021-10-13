package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.BatchContract
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.states.BatchState
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object TestBatchRecordFlow {

    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam : BatchState
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
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)

            // We create the transaction components.
            progressTracker.currentStep = TX_COMPONENTS

            val lspName: CordaX500Name = CordaX500Name(
                    organisation = "LSP",
                    locality = "Port-au-Prince",
                    country = "HT")
            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collectorName: CordaX500Name = CordaX500Name(
                    organisation = "Collector",
                    locality = "Port-au-Prince",
                    country = "HT")
            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val brokerName: CordaX500Name = CordaX500Name(
                    organisation = "Broker",
                    locality = "New York",
                    country = "US")
            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val saeName: CordaX500Name = CordaX500Name(
                    organisation = "SAE",
                    locality = "Port-au-Prince",
                    country = "HT")
            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val outputState = reqParam
            val command = Command(BatchContract.BatchCommands.TestBatchRecord(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(outputState, BatchContract.ID)
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
            val saesession = initiateFlow(sae)

            return subFlow(FinalityFlow(signedTx, listOf(lspSession, collectorSession, saesession)))

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