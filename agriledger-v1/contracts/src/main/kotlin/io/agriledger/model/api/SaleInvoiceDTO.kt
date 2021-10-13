package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class SaleInvoiceDTO(val invoiceNo: String,
                     val billingDate: String,
                     val brokerName: String,
                     val brokerOrganization: String,
                     val buyerName: String,
                     val buyerOrganization: String,
                     val noofBoxesPurchased: String,
                     val product: String,
                     val pricePerKg: String,
                     val pricePerKgCurrency: String,
                     val GICurrency: String,
                     val netSales: String,
                     val aproximateWeightOfProduct: String,
                     val brokerPercent: String,
                     val brokerTransportFlatFee: String
)