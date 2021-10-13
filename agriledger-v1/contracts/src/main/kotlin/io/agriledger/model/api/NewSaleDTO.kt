package io.agriledger.model.api

import io.agriledger.model.LotSaleModel
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class NewSaleDTO(
    val saleId: String,
    val batchId: String,
    val sellBuyerName: String,
    val sellBuyerAddress: String,
    val sellBuyerOrganization: String,
    val sellBuyerContactDetails: String,
    val sellBuyerEmailAddress: String,
    val saleTransactionDate: String,
    val totalNoOfBoxesSold: String,
    val soldProduct: String,
    val salesPricePerKg: String,
    val salesWeightPerBox: String,
    val salesCurrency: String,
    val salesBrokerMargin: String,
    val lots: MutableList<LotSaleModel>,
    val lspOrganizationName: String,
    val lspOrganizationKey: String,
    val lspPercentageDistribution: String,
    val brokerOrganizationName: String,
    val brokerOrganizationKey: String,
    val avgWeight: String,
    val ratePerKg: String,
    val tspMargin: String,
    val tspOrganizationName: String,
    val tspOrganizationKey: String
)