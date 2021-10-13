package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesRemovalFromForcedAirCoolingDTO(val fruitTemperatureOnExit: String,
                                                    val mandatory: String,
                                                    val timeOfExit: String
)
