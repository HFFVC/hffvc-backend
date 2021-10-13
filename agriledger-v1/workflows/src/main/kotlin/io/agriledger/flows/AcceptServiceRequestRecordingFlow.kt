package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

object AcceptServiceRequestRecordingFlow {
    // *********
// * Flows *
// *********
    // val reqParam: AcceptServiceRequestDTO
    @InitiatingFlow
    class Initiator(val fullySignedTransaction: SignedTransaction, val parties: List<Party>) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val sessions = parties.map { initiateFlow(it) }
            return subFlow(FinalityFlow(fullySignedTransaction, sessions))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            subFlow(ReceiveFinalityFlow(counterpartySession))
        }
    }
}