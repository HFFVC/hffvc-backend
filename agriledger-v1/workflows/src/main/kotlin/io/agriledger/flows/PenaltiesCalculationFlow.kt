package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.CheckForPenalties
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.api.PenaltiesDTO
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception

object PenaltiesCalculationFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: PenaltiesDTO, val email: String?) : FlowLogic<SignedTransaction>() {
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

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            progressTracker.currentStep = EXTRACTING_VAULT_STATES


            val wantedStateRef = QueryVault().queryServiceRequestById(reqParam.id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Service request ${reqParam.id} not found.")
            val vaultData = wantedStateRef.state.data

            if (vaultData.broker != ourIdentity) {
                throw IllegalArgumentException("Could not proceed with flow execution. The party is not authorised to run this flow.")
            }


            progressTracker.currentStep = TX_COMPONENTS

            val newSignatories = vaultData.signatories.toMutableList()
            newSignatories.add(ourIdentity)


            // calculating penalties

            val checkPenalties = CheckForPenalties()
            val outputState = checkPenalties.check(vaultData)

            val command = Command(ServiceRequestContract.Commands.CalculatePenalty(), listOf(ourIdentity.owningKey))

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

            val initiatingPartySession = initiateFlow(ourIdentity)
            val fullySignedTransaction: SignedTransaction = subFlow(CollectSignaturesFlow(signedTx, listOf(initiatingPartySession)))
            val otherParties = listOf<Party>(lsp, sae, collector, broker).filter { it.name != ourIdentity.name }

            return subFlow(PenaltiesRecordingFlow.Initiator(fullySignedTransaction, otherParties))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            // Responder flow logic goes here.
            val signedTransaction = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {

                }
            }
            subFlow(signedTransaction)
        }
    }
}