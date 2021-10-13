package io.agriledger.model

import io.agriledger.model.enumerations.Product
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class BrokerInvoiceModel(val saleId: String,
                         val invoiceNo: String,
                         val billingDate: LocalDateTime,
                         val brokerName: String,
                         val brokerOrganization: String,
                         val buyerName: String,
                         val buyerOrganization: String,
                         val product: Product,
                         val boxesSold: Int,
                         val pricePerKgCurrency: String,
                         val pricePerKg: Float,
                         val aproximateWeightOfProduct: Float,
                         val GICurrency: String,
                         val netSales: Float,
                         val emailId: String
)