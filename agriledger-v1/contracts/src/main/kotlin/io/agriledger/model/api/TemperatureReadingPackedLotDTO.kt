package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TemperatureReadingPackedLotDTO(val ambienttemperaturePacked: String?
                                     , val internalFruitTemperaturePacked: String?
)