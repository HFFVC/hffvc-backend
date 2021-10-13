package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesTemperatureReadingPackedLotDTO(val ambientTemperature: String,
                                                    val internalFruitTemperature: String,
                                                    val mandatory: String
)
