package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class WithProducerDTO(val id: String,
                      val status: String,
                      val temperature: String,
                      val ambientTemp: String,
                      val crates: String,
                      val fruitsHarvested: String,
                      val fruitRejected: String,
                      val advanceGiven: String,
                      val currency: String,
                      val startingQRCode: String,
                      val endingQRCode: String,
                      val paymentMethod: String,
                      val wirePayment: WirePaymentDTO?,
                      val cashPayment: CashPaymentDTO?,
                      val mobilePayment: MobilePaymentDTO?,
                      val AdditionalNotes: String?,
                      val withProducerQrCodeFile: MutableList<String>,
                      val withProducerTimeStamp: String,
                      val ngo: String?,
                      val conversionRate: String?,
                      val conversionCurrency: String?,
                      val lspAdvanceGivenCurrency: String?,
                      val lspAdvanceGiven: String?
)