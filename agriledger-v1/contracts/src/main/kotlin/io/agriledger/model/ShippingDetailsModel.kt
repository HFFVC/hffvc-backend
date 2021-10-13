package io.agriledger.model

import io.agriledger.model.enumerations.Shipping
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ShippingDetailsModel(val shippingDetails: Shipping? = null, val ratePerKg: Float? = null)