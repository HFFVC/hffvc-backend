package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class TransferToColdStorageModel(val temperatureTransfer: Float? = null,
                                 val coldStorageTimeIn: LocalDateTime? = null
)