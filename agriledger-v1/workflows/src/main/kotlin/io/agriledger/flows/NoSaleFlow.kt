package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.api.*
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception

object NoSaleFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: NoSaleDTO, val email: String?) : FlowLogic<SignedTransaction>() {
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
                    //SIGS_GATHERING,
                    //VERIFYING_SIGS,
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

            // getting the batch to update
            val wantedStateRef = QueryVault().queryBatchById(reqParam.batchId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            if (wantedStateRef == null) {
                throw Exception("Batch ID does not exist.")
            }

            // getting all service request ids
            val allWantedStateRefs = QueryVault().queryServiceRequestByBatch(reqParam.batchId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            //val allWantedStates = allWantedStateRefs.map { it.state.data }
            val inputStates = mutableListOf<StateAndRef<ServiceRequestState>>()
            val allServiceRequestOutputStates = mutableListOf<ServiceRequestState>()
            allWantedStateRefs.forEach {
                val usernames = it.state.data.usernames.toMutableSet()
                usernames.add(email!!)
                "before the condition checking!!!!!!!!!!!".also(::println)
                if (it.state.data.creationData.status != "SOLD_OUT") {
                    "coming here********".also(::println)
                    inputStates.add(it)
                    "value of it ${it.toString()}".also(::println)
                    allServiceRequestOutputStates.add(
                            it.state.data.copy(
                                    creationData = it.state.data.creationData.copy(
                                            status = reqParam.status
                                    ),
                                    usernames = usernames
                            )
                    )
                }
            }

            progressTracker.currentStep = TX_COMPONENTS


            val command = Command(ServiceRequestContract.Commands.NoSale(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
            inputStates.map { txBuilder.addInputState(it) }
            allServiceRequestOutputStates.map { txBuilder.addOutputState(it, ServiceRequestContract.ID) }
            txBuilder.addCommand(command)


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

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val saeSession = initiateFlow(sae)
            val collectorSession = initiateFlow(collector)
            val lspSession = initiateFlow(lsp)

            return subFlow(FinalityFlow(signedTx, listOf(saeSession, collectorSession, lspSession)))
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