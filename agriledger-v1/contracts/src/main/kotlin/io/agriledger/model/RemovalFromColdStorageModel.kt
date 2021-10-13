package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class RemovalFromColdStorageModel(val temperatureRemoval: Float? = null,
                                  val coldStorageTimeOut: LocalDateTime? = null
)