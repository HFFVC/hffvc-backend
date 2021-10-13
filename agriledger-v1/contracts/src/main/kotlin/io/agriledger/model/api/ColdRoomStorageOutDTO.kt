package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ColdRoomStorageOutDTO(val fruitTemperatureColdStorageOut: String?
                            , val storageTimeOut: String?
)