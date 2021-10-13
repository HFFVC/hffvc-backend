package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EmailAttachmentDTO(
    val base64EncodedString: String,
    val filename: String,
    val type: String
)