package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class NodeExplorerServiceRequestModel(val id: String,
                                      val createdOn: LocalDateTime
)