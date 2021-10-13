package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesHydroCoolingExitDTO(val duration: String,
                                         val internalFruitTemperature: String,
                                         val mandatory: String,
                                         val timeOfExit: String
)
