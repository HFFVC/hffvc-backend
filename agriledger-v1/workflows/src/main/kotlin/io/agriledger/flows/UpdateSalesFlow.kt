package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.model.api.UpdateSalesDTO
import io.agriledger.model.enumerations.SalesStep
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import java.lang.Exception

object UpdateSalesFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: UpdateSalesDTO, val email: String?) : FlowLogic<SignedTransaction>() {


        @Suspendable
        override fun call(): SignedTransaction {
            when (reqParam.step) {
                SalesStep.SHIP_ORDER -> {
                    return subFlow(ShipOrderFlow.Initiator(reqParam.saleId, reqParam.shipOrder, email))
                }
                SalesStep.UNLOADING_AT_BUYER -> {
                    return subFlow(UnloadingAtBuyerFlow.Initiator(reqParam.saleId, reqParam.unloadingAtBuyer, email))
                }
                SalesStep.SALE_INVOICE -> {
                    return subFlow(SalesInvoiceFlow.Initiator(reqParam.saleId, reqParam.saleInvoice, email))
                }
                SalesStep.CONFIRM_PAYMENT -> {
                    return subFlow(ConfirmPaymentFlow.Initiator(reqParam.saleId, reqParam.confirmPayment, email))
                }
                else -> {
                    throw Exception("Invalid step in batch Sales!")
                }
            }
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            // Responder flow logic goes here.

        }
    }
}