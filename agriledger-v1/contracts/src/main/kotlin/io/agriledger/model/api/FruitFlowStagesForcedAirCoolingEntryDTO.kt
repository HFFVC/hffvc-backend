package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesForcedAirCoolingEntryDTO(val airflowTemperature: String,
                                              val fruitTemperatureAtEntry: String,
                                              val mandatory: String,
                                              val timeOfEntry: String
)
