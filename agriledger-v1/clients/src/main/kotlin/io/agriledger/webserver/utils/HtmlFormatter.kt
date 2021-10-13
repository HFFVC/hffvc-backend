package io.agriledger.webserver.utils

import io.agriledger.model.EscrowEmailModel
import io.agriledger.model.api.UserCreationEmailDTO
import io.agriledger.webserver.models.EmailContentsModel
import java.time.format.DateTimeFormatter

class HtmlFormatter {
    fun proformaInvoiceMailTemplate(data: EmailContentsModel): String {
        val emailTemplate = """
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office"><head></head><body>
    Hello ${data.fullName}, <br /><br />
Greetings! <br /><br />


We are excited to have you onboard for your journey to <b>"Building Trust”</b> in the Agricultural Value Chain. By selling these fruits you will be supporting farmers in earning a fair share from their produce.<br /><br />


Please find attached your proforma invoice for <i>${data.invoiceNo}</i> with this mail.<br /><br />

If you have any problems viewing the details or any other queries, please write to us at  <a href="mailto:${ConfigConstants.configData.supportEmail}"> ${ConfigConstants.configData.supportEmail} </a> <br /><br />

Kind regards,<br />
${data.sellerName}
</body></html>""".trimIndent()
        return emailTemplate
    }

    fun newUserEmailTemplate(data: UserCreationEmailDTO): String {
        val emailTemplate = """
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office"><head></head><body>
    Hi ${data.userFullName}, <br /><br />
Welcome to your AgriLedger account set up! We are excited to have you onboard for your journey to <b>"Building Trust”</b> in the Agricultural Value Chain. <br /><br />
 
Please find your login details below<br />
<b>User Name: </b> ${data.emailId} <br />
<b>Password: </b> ${data.password} <br /><br />
 
Please click here <a href="${ConfigConstants.configData.baseUrl}"> ${ConfigConstants.configData.baseUrl}</a>  to log into your account.<br /><br />
 
If you have any problems signing in or any other queries, please write to us at <a href="mailto:${ConfigConstants.configData.supportEmail}"> ${ConfigConstants.configData.supportEmail} </a> <br /><br />
 
Kind regards,<br />
Agriledger UK Ltd.<br />
<br /></body></html>""".trimIndent()
        return emailTemplate
    }

    fun proformaInvoice(data: EmailContentsModel): String {
        val invoiceTemplate = """
<table style="width: 100%;" border="1">
              <tr>
                <td>Proforma Number</td>
                <td>${data.invoiceNo}</td>
              </tr>
              <tr>
                <td>Seller Name</td>
                <td>${data.sellerName}</td>
              </tr>
              <tr>
                <td>Packhouse</td>
                <td>${data.dropLocation}</td>
              </tr>
              <tr>
                <td>Product</td>
                <td>${data.product}</td>
              </tr>
              <tr>
                <td>Unit Price</td>
                <td>${data.unitPrice}</td>
              </tr>
              <tr>
                <td>Currency</td>
                <td>${data.proformaInvoiceCurrency}</td>
              </tr>
              <tr>
                <td>Selling To</td>
                <td>${data.fullName}</td>
              </tr>
              <tr>
                <td>Email Id</td>
                <td>${data.email}</td>
              </tr>
              <tr>
                <td>Address Line 1</td>
                <td>${data.sellingAddressLine1}</td>
              </tr>
              <tr>
                <td>Address Line 2</td>
                <td>${data.sellingAddressLine2}</td>
              </tr>
              <tr>
                <td>Address Line 3</td>
                <td>${data.sellingAddressLine3}</td>
              </tr>
              <tr>
                <td>Shipping Address</td>
                <td></td>
              </tr>
              <tr>
                <td>Address Line 1</td>
                <td>${data.shippingAddressLine1}</td>
              </tr>
              <tr>
                <td>Address Line 2</td>
                <td>${data.shippingAddressLine2}</td>
              </tr>
              <tr>
                <td>Address Line 3</td>
                <td>${data.shippingAddressLine3}</td>
              </tr>
            </table>""".trimIndent()
        return invoiceTemplate
    }

    fun brokerInvoiceMailTemplate(data: EmailContentsModel): String {
        val emailTemplate = """
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office"><head></head><body>
    Hello ${data.buyerName}, <br /><br />

Please find attached the invoice from ${data.brokerOrganization} for your purchase ${data.saleId} on ${data.billingDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}. <br /><br />


The products you purchased have an individual QR code attached, that will allow the consumer full traceability from the farm to the sale to you, including name of farmer, location, timestamps and temperatures. It can also display the distribution of revenue. Please click <a href="${ConfigConstants.configData.baseUrl}/buyer-screen/${data.saleId}">here</a> if you would like to make the distribution available to the consumers: <br /><br />

If you have any problems viewing the details or any other queries, please write to us at  <a href="mailto:haiti_support@agriledger.com"> haiti_support@agriledger.com </a> <br /><br />

Kind regards,<br />
${data.brokerName}<br />
${data.brokerOrganization}<br />
${data.emailId}
</body></html>""".trimIndent()
        return emailTemplate
    }

    fun brokerInvoice(data: EmailContentsModel): String {
        val invoiceTemplate = """<table
              style="width: 100%;"
              border="1"
            >
              <tr>
                <td>Invoice Number</td>
                <td>${data.invoiceNo}</td>
              </tr>
              <tr>
                <td>Billing Date</td>
                <td>${data.billingDate}</td>
              </tr>
              <tr>
                <td>Broker Name</td>
                <td>${data.brokerName}</td>
              </tr>
              <tr>
                <td>Broker Organization</td>
                <td>${data.brokerOrganization}</td>
              </tr>
              <tr>
                <td>Buyer Name</td>
                <td>${data.buyerName}</td>
              </tr>
              <tr>
                <td>Buyer Organization</td>
                <td>${data.buyerOrganization}</td>
              </tr>
              <tr>
                <td>Product</td>
                <td>${data.product}</td>
              </tr>
              <tr>
                <td>No. of Boxes Purchased</td>
                <td>${data.boxesSold}</td>
              </tr>
              <tr>
                <td>Currency</td>
                <td>${data.pricePerKgCurrency}</td>
              </tr>
              <tr>
                <td>Price Per Kg</td>
                <td>${data.pricePerKg}</td>
              </tr>
              <tr>
                <td>Approximate Weight Of Product Sold</td>
                <td>${data.aproximateWeightOfProduct}</td>
              </tr>
              <tr>
                <td>Currency</td>
                <td>${data.GICurrency}</td>
              </tr>
              <tr>
                <td>Net Sales</td>
                <td>${data.netSales}</td>
              </tr>
            </table>""".trimIndent()
        return invoiceTemplate
    }

    fun escrowAccountTemplate(data: EscrowEmailModel): String {
        return """
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office"><head></head><body>
   Hi ${data.brokerName}, <br><br>
   Please find LSP Escrow Account details below: <br><br>
   <b>LSP Organization Name:</b>${data.lspOrganizationName}<br>
   <b>Escrow Account No:</b>${data.escrowAccountNo}<br>
   <b>Net Payable:</b>${data.netPayable}
   <br><br>
   Thank You.


</body></html>""".trimIndent()
    }

    fun formatEmail(data: String): String {

        return """
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office"><head></head><body>
   ${data}
</body></html>""".trimIndent()
    }
}