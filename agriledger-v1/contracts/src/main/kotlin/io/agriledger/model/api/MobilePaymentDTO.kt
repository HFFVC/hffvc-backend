package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class MobilePaymentDTO(val mobilePaymentOperator: String,
                       val mobilePaymentNumber: String,
                       val mobilePaymentCurrency: String
)