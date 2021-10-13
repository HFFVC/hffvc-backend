package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesTransferToColdStorageDTO(val mandatory: String,
                                              val storageTimeIn: String,
                                              val temperature: String
)
