package io.agriledger.webserver.models

import com.sendgrid.helpers.mail.objects.Attachments
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EmailDataModel(
                     val from: String,
                     val fromName: String,
                     val to: MutableSet<String>,
                     val cc: MutableSet<String> = mutableSetOf(),
                     val subject: String,
                     val emailTemplate: String,
                     val attachments: MutableList<Attachments> = mutableListOf()
)