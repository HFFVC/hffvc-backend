package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class WithProducerModel(val temperature: Float
                             , val ambientTemp: Float
                             , val crates: Float
                             , val fruitsHarvested: Int
                             , val fruitRejected: Int
                             , val advanceGiven: Float
                             , val currency: String
                             , val startingQRCode: String
                             , val endingQRCode: String
                             , val paymentMethod: String
                             , val cashPaymentData: CashPaymentModel? = null
                             , val mobilePaymentData: MobilePaymentModel? = null
                             , val wirePaymentData: WirePaymentModel? = null
                             , val AdditionalNotes: String?
                             , val withProducerQrCodeFile: MutableList<String>
                             , val withProducerTimeStamp: LocalDateTime
                             , val ngo: String? = null
                             , val conversionRate: Float? = null
                             , val conversionCurrency: String? = null
                             , val lspAdvanceGivenCurrency: String? = null
                             , val lspAdvanceGiven: Float? = null
)


