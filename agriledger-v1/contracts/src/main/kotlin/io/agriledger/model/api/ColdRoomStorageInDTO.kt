package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ColdRoomStorageInDTO(val fruitTemperatureColdStorage: String?
                           , val airflowTemperatureRHColdStorage: String?
                           , val storageTimeIn: String?
)