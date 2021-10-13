package io.agriledger.webserver.utils

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Attachments
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import io.agriledger.webserver.models.EmailDataModel
import java.io.IOException

class SendEmail {

    fun sendEmail(emailData: EmailDataModel) {
        val from = Email(emailData.from, emailData.fromName)
        val subject = emailData.subject
        val content = Content("text/html", emailData.emailTemplate)
        val mail = Mail(from, subject, Email(emailData.to.first()), content)
        val personalization = Personalization()
        emailData.to.map { toAddress -> personalization.addTo(Email(toAddress)) }
//        personalization.addTo(Email(emailData.to))
        ConfigConstants.configData.ccAddresses.forEach {
            personalization.addCc(Email(it))
        }
        emailData.cc.map { ccAddress -> personalization.addCc(Email(ccAddress)) }

        val sendGrid = SendGrid(ConfigConstants.configData.sendGridKey)
        emailData.attachments.forEach {
            mail.addAttachments(it)
        }

        mail.addPersonalization(personalization)

        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sendGrid.api(request)
            println(response.statusCode)
            println(response.body)
            println(response.headers)
        } catch (e: IOException) {
            println(e.toString())
            throw e
        }
    }

    fun generateEmailAttachment(encodedContent: String, filename: String, type: String): Attachments {
        val attachment = Attachments()
        attachment.content = encodedContent
        attachment.disposition = "attachment"
        attachment.filename = filename
        attachment.type = type

        return attachment
    }

}