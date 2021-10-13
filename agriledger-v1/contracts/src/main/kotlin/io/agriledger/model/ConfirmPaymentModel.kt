package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ConfirmPaymentModel(val confirmPaymentDate: LocalDateTime,
                          val netReceivables: Float,
                          val paymentNetReceivablesCurrency: String,
                          val wasFactored: Boolean,
                          val amountFactored: Float?,
                          val factoringEntity: String?,
                          val factoringAgent: String?,
                          val factoringContactDetails: String?,
                          val factoringCharges: Float?,
                          val escrowAccountNo: String? = null,
                          val brokerBankTransactionCurrency: String? = null,
                          val brokerBankTransactionFee: Float? = null,
                          val netPayable: Float? = null
)