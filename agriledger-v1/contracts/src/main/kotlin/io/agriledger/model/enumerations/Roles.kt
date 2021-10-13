package io.agriledger.model.enumerations
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class Roles {
    SAE,
    LSP,
    COLLECTOR,
    BROKER,
    TSP
}