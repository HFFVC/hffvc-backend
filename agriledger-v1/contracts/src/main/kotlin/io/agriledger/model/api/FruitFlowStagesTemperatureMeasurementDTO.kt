package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesTemperatureMeasurementDTO(val ambientTemp: String,
                                               val attachments: String,
                                               val internalFruitTemperature: String,
                                               val mandatory: String
)
