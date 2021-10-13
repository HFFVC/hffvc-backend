package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ConfirmPaymentDTO(val confirmPaymentDate: String,
                        val netReceivables: String,
                        val paymentNetReceivablesCurrency: String,
                        val wasFactored: String,
                        val amountFactored: String?,
                        val factoringEntity: String?,
                        val factoringAgent: String?,
                        val factoringContactDetails: String?,
                        val factoringCharges: String?,
                        val escrowAccountNo: String,
                        val brokerBankTransactionCurrency: String,
                        val brokerBankTransactionFee: String,
                        val netPayable: String
)