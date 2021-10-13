package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesRemovalFromColdStorageDTO(val mandatory: String,
                                               val storageTimeOut: String,
                                               val temperature: String
)
