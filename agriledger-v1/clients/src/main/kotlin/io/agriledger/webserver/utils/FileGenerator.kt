package io.agriledger.webserver.utils

import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.util.*

class FileGenerator {

    fun convertHtmlToPdf(htmlContent: String): String {
        val outputStream = ByteArrayOutputStream()

        HtmlConverter.convertToPdf(htmlContent, PdfWriter(outputStream))
        println(outputStream.size())

        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }
}