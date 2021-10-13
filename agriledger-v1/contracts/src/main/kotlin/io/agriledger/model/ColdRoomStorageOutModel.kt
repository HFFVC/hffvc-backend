package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ColdRoomStorageOutModel(val fruitTemperatureColdStorageOut: Float? = null
                              , val storageTimeOut: LocalDateTime? = null
)