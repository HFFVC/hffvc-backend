package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagescoldRoomStorageOutDTO(val fruitTemperature: String,
                                           val mandatory: String,
                                           val storageTimeOut: String
)