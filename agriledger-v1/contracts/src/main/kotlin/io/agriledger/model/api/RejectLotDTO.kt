package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class RejectLotDTO(val id: String,
                   val status: String,
                   val rejectedOn: String,
                   val rejectReason: String,
                   val rejectAddionalNote: String
)