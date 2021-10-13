package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesGradingDTO(val fruitsRemoved: String,
                                val gradingResults: String,
                                val mandatory: String
)
