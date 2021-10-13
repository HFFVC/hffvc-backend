package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class MobilePaymentModel(val mobilePaymentOperator: String,
                         val mobilePaymentNumber: String,
                         val mobilePaymentCurrency: String
)