package io.agriledger.model

import io.agriledger.model.enumerations.Product
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class SalesInvoiceModel(val invoiceNo: String,
                        val billingDate: LocalDateTime,
                        val brokerName: String,
                        val brokerOrganization: String,
                        val buyerName: String,
                        val buyerOrganization: String,
                        val noofBoxesPurchased: Int,
                        val product: Product,
                        val pricePerKg: Float,
                        val pricePerKgCurrency: String,
                        val GICurrency: String,
                        val netSales: Float,
                        val aproximateWeightOfProduct: Float,
                        val brokerPercent: Float,
                        val brokerTransportFlatFee: Float
)