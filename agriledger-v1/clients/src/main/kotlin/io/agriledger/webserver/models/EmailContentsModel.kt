package io.agriledger.webserver.models

import io.agriledger.model.enumerations.Currency
import io.agriledger.model.enumerations.Product
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class EmailContentsModel(val invoiceNo: String? = null,
                         val sellerName: String? = null,
                         val dropLocation: String? = null,
                         val product: Product? = null,
                         val unitPrice: Float? = null,
                         val proformaInvoiceCurrency: String? = null,
                         val fullName: String? = null,
                         val email: String? = null,
                         val sellingAddressLine1: String? = null,
                         val sellingAddressLine2: String? = null,
                         val sellingAddressLine3: String? = null,
                         val shippingAddressLine1: String? = null,
                         val shippingAddressLine2: String? = null,
                         val shippingAddressLine3: String? = null,

                         val saleId: String? = null,
                         val billingDate: LocalDateTime? = null,
                         val brokerName: String? = null,
                         val brokerOrganization: String? = null,
                         val buyerName: String? = null,
                         val buyerOrganization: String? = null,
                         val boxesSold: Int? = null,
                         val pricePerKgCurrency: String? = null,
                         val pricePerKg: Float? = null,
                         val aproximateWeightOfProduct: Float? = null,
                         val GICurrency: String? = null,
                         val netSales: Float? = null,
                         val emailId: String? = null

)