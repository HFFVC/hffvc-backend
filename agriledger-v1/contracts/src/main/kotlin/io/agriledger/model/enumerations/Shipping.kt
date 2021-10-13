package io.agriledger.model.enumerations

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class Shipping {
    SHIPMENT_BY_AIR,
    SHIPMENT_BY_SEA
}