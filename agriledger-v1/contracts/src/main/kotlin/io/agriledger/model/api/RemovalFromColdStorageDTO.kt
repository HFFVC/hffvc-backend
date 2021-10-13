package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class RemovalFromColdStorageDTO(val temperatureRemoval: String?,
                                val coldStorageTimeOut: String?
)