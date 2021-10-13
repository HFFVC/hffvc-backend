package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.model.api.UpdateBatchDTO
import io.agriledger.model.enumerations.UpdateBatchSteps
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import java.lang.Exception

object UpdateBatch {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: UpdateBatchDTO, val email: String?) : FlowLogic<SignedTransaction>() {


        @Suspendable
        override fun call(): SignedTransaction {
            when (reqParam.step) {
                UpdateBatchSteps.ARRIVAL_AND_DESTINATION -> {
                    return subFlow(ArrivalAndDestinationFlow.Initiator(reqParam.batchId, reqParam.arrivalAndDestination, email))
                }
                UpdateBatchSteps.QUALITY_INSPECTION -> {
                    return subFlow(BatchQualityInspectionFlow.Initiator(reqParam.batchId, reqParam.qualityInspection, email))
                }
                UpdateBatchSteps.COLD_STORAGE -> {
                    return subFlow(BatchColdStorageFlow.Initiator(reqParam.batchId, reqParam.coldStorage, email))
                }
                UpdateBatchSteps.COST_OF_MATURATION -> {
                    return subFlow(CostOfMaturationFlow.Initiator(reqParam.batchId, reqParam.costOfMaturation, email))
                }
                else -> {
                    throw Exception("Invalid step in batch update flow!")
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