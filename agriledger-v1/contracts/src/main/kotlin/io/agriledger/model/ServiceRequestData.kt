package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ServiceRequestData(val id: String,
                         val displayId: String?,
                         val createdOn: LocalDateTime
)

