package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TemperatureReadingPackedLotModel(val ambienttemperaturePacked: Float? = null
                                       , val internalFruitTemperaturePacked: Float? = null
)