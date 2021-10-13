package io.agriledger.model.enumerations

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class UpdateBatchSteps {
    ARRIVAL_AND_DESTINATION,
    QUALITY_INSPECTION,
    COLD_STORAGE,
    COST_OF_MATURATION
}