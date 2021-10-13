package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EmailDataDTO(
    val to: MutableSet<String>,
    val cc: MutableSet<String>?,
    val subject: String,
    val emailBody: String,
    val attachments: MutableList<EmailAttachmentDTO>?
)