package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class BatchColdStorageModel(val temperature: Float,
                            val phLevel: Float,
                            val ethyleneLevel: Float,
                            val co2Level: Float,
                            val coldStorageInTimestamp: LocalDateTime
)
