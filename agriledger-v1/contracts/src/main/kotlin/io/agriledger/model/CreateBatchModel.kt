package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class CreateBatchModel(val batchId: String,
                       val batchCreatedAt: LocalDateTime,
                       val lotIds: MutableList<String>
)