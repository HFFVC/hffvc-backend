package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ProducerData(val serviceRequestId: String,
                   val displayId: String?,
                   val farmerName: String,
                   val identificationNo: String? = null,
                   val location: String,
                   val phoneNo: String? = null,
                   val ngo: String? = null,
                   val advanceGiven: Float? = null,
                   val exchangeRate: Float? = null,
                   val amountPayable: Float? = null,
                   val amountToNgo: Float? = null,
                   val amountToProducer: Float = 0f,
                   val paymentMode: String? = null,
                   val cashPaymentData: CashPaymentModel? = null,
                   val mobilePaymentData: MobilePaymentModel? = null,
                   val wirePaymentData: WirePaymentModel? = null,
                   val penalties: MutableList<PenaltyDataModel> = mutableListOf()
)