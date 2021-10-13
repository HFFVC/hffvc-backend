package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagescoldRoomStorageInDTO(val airflowTemperature: String,
                                          val fruitTemperature: String,
                                          val mandatory: String,
                                          val storageTimeIn: String
)