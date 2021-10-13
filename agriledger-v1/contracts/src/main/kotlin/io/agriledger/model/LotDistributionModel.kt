package io.agriledger.model

import io.agriledger.model.enumerations.Currency
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class LotDistributionModel(val lotId: String,
                           val displayId: String?,
                           val producerName: String,
                           val identificationNo: String? = null,
                           val location: String,
                           val phoneNo: String? = null,
                           val boxesSold: Int,
                           val producerAmount: Float,
                           val advanceConsidered: Float? = null,
                           val exchangeRate: Float? = null,
                           val ngoName: String?,
                           val ngoAmount: Float?,
                           val currency: Currency,
                           val paymentMode: String? = null,
                           val cashpaymentData: CashPaymentModel? = null,
                           val mobilePaymentData: MobilePaymentModel? = null,
                           val wirePaymentData: WirePaymentModel? = null,
                           val penalties: MutableList<PenaltyDataModel> = mutableListOf()
)