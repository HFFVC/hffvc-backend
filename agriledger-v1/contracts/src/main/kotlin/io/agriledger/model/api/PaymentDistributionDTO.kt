package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class PaymentDistributionDTO(val saleId: String,
                             val serviceRequestId: String
)