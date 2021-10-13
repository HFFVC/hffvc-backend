package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TemperatureMeasurementModel(val ambientTemp: Float? = null
                                  , val internalFruitTemp: Float? = null
                                  , val isTemepertaureBreach: Boolean? = null
                                  , val temperatureBreachCount: Int? = null
)