package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CashPaymentModel(val cashPaymentCurrency: String,
                       val farmerVoucher: String,
                       val contactnumber: String
)