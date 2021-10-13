package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class RejectLotModel(val rejectedOn: LocalDateTime,
                     val rejectReason: String,
                     val rejectAddionalNote: String
)