package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CashPaymentDTO(val cashPaymentCurrency: String,
                     val farmerVoucher: String,
                     val contactnumber: String
)