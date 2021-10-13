package io.agriledger.model.enumerations

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class SalesStep {
    SHIP_ORDER,
    UNLOADING_AT_BUYER,
    SALE_INVOICE,
    CONFIRM_PAYMENT
}