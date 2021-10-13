package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ColdRoomStorageInModel(val fruitTemperatureColdStorage: Float? = null
                             , val airflowTemperatureRHColdStorage: Float? = null
                             , val storageTimeIn: LocalDateTime? = null
)