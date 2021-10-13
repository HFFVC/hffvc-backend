package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class FlaggedTransactionModel(
    val saleId: String,
    val batchId: String,
    val confirmPaymentDate: LocalDateTime? = null,
    val amount: Float,
    val receivingPerson: String,
    val serviceRendered: String,
    val identificationNo: String? = null,
    val location: String? = null
)