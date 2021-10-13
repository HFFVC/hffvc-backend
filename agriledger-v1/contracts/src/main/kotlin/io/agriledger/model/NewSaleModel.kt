package io.agriledger.model

import io.agriledger.model.enumerations.Product
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class NewSaleModel(
    val sellBuyerName: String,
    val sellBuyerAddress: String,
    val sellBuyerOrganization: String,
    val sellBuyerContactDetails: String,
    val sellBuyerEmailAddress: String,
    val saleTransactionDate: LocalDateTime,
    val totalNoOfBoxesSold: Int,
    val soldProduct: Product,
    val salesPricePerKg: Float,
    val salesWeightPerBox: Float,
    val salesCurrency: String,
    val salesBrokerMargin: Float,
    val lspOrganizationName: String? = null,
    val lspOrganizationKey: String? = null,
    val lspPercentageDistribution: Float? = null,
    val brokerOrganizationName: String? = null,
    val brokerOrganizationKey: String? = null,
    val avgWeight: Float? = null,
    val ratePerKg: Float? = null,
    val tspMargin: Float? = null,
    val tspOrganizationName: String? = null,
    val tspOrganizationKey: String? = null
)