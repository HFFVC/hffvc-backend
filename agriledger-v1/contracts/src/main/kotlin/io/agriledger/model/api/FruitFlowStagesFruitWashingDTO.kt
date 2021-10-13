package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesFruitWashingDTO(val PHLevel: String,
                                     val chlorineLevel: String,
                                     val mandatory: String,
                                     val waterTemperature: String
)