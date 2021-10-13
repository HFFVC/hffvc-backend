package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TemperatureMeasurementDTO(val ambientTemp: String?,
                                val internalFruitTemp: String?,
                                val isTemepertaureBreach: String?,
                                val temperatureBreachCount: String?
)