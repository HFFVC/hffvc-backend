package io.agriledger.flows

import io.agriledger.flows.utils.Constants
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.LotDistributionModel
import io.agriledger.model.PaymentDistributionModel
import io.agriledger.model.ProducerData
import io.agriledger.model.SalesDistributionModel
import io.agriledger.model.enumerations.Currency
import io.agriledger.model.enumerations.Roles
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.VaultService
import kotlin.Exception

object PaymentDist {

    class Distribution(val saleId: String) {
        fun calculateDistribution(vaultService: VaultService): PaymentDistributionModel {

            val wantedStateRef = QueryVault().querySalesById(saleId, Vault.StateStatus.UNCONSUMED, vaultService)
                ?: throw Exception("Sale ID $saleId not found.")

            val vaultData = wantedStateRef.state.data
//            var totalAdvanceGiven = 0f
            var totalLSPAdvanceGiven = 0f
            val producerData = mutableListOf<ProducerData>()
            val allServiceRequests = vaultData.lotsSold.map {
                QueryVault().queryServiceRequestById(
                    it.id,
                    Vault.StateStatus.UNCONSUMED,
                    vaultService
                )
            }
            allServiceRequests.forEach {
                if (it != null) {
                    val vault = it.state.data
                    val advanceNonConverted = vault.withProducerData?.advanceGiven
                    val producerAdvanceGivenCurrency = vault.withProducerData?.currency
                    val lspAdvanceNonConverted = vault.withProducerData?.lspAdvanceGiven
                    val conversionCurrency = vault.withProducerData?.conversionCurrency
                    val lspAdvanceGivenCurrency = vault.withProducerData?.lspAdvanceGivenCurrency
                    val conversionRate = vault.withProducerData?.conversionRate
                    val cin = vault.creationData.cin
                    val nif = vault.creationData.nif
                    var identificationNo: String? = null
                    if (cin != null) {
                        for (i in cin) {
                            if (i == '0') {
                                identificationNo = "INVALID"
                            } else {
                                identificationNo = cin
                                break
                            }
                        }
                    }
                    if (identificationNo == null || identificationNo == "INVALID") {
                        if (nif != null) {
                            for (i in nif) {
                                if (i == '0') {
                                    identificationNo = "INVALID"
                                } else {
                                    identificationNo = nif
                                    break
                                }
                            }
                        }

                    }
                    val advance =
                        if (
                            advanceNonConverted != null &&
                            conversionCurrency == "USD" &&
                            conversionRate != null &&
                            vault.firstSale == saleId
                        ) {
                            if (producerAdvanceGivenCurrency == "HTG") {
                                advanceNonConverted / conversionRate
                            } else {
                                advanceNonConverted
                            }

                        } else {
                            null
                        }
                    val lspAdvance =
                        if (
                            lspAdvanceNonConverted != null &&
                            conversionCurrency == "USD" &&
                            conversionRate != null &&
                            vault.firstSale == saleId
                        ) {
                            if (lspAdvanceGivenCurrency == "HTG") {
                                lspAdvanceNonConverted / conversionRate
                            } else {
                                lspAdvanceNonConverted
                            }

                        } else {
                            null
                        }

//                    if (advance != null) {
//                        totalAdvanceGiven = advance
//                    }
                    if (lspAdvance != null) {
                        totalLSPAdvanceGiven += lspAdvance
                    }
                    producerData.add(
                        ProducerData(
                            serviceRequestId = vault.id,
                            farmerName = vault.creationData.producer,
                            identificationNo = identificationNo,
                            location = vault.creationData.location,
                            phoneNo = vault.creationData.phoneNo,
                            ngo = vault.withProducerData?.ngo,
                            advanceGiven = advance,
                            exchangeRate = conversionRate,
                            penalties = vault.penaltiesIncurred.toMutableList(),
                            displayId = vault.creationData.displayId,
                            paymentMode = vault.withProducerData?.paymentMethod,
                            cashPaymentData = vault.withProducerData?.cashPaymentData,
                            mobilePaymentData = vault.withProducerData?.mobilePaymentData,
                            wirePaymentData = vault.withProducerData?.wirePaymentData
                        )
                    )
                }
            }
            println("total lsp advance given $totalLSPAdvanceGiven")

            val totalBoxesSold = vaultData.creationData.totalNoOfBoxesSold
            println("Total boxes sold =  $totalBoxesSold")
            val brokerMargin = vaultData.creationData.salesBrokerMargin / 100
            println("broker margin = $brokerMargin")
            var transportationFee = 0f
            if (vaultData.shipOrderData != null && vaultData.shipOrderData?.sellCostOfTransportation != null) {
                transportationFee = vaultData.shipOrderData?.sellCostOfTransportation!!
            }
            println("transportation fee = $transportationFee")
            val pricePerKg = vaultData.creationData.salesPricePerKg
            println("price per kg = $pricePerKg")
            val soldFruit = vaultData.creationData.soldProduct


            val avgWeight = vaultData.creationData.avgWeight ?: throw Exception("Average weight not present.")

            var netSales = (totalBoxesSold * avgWeight * pricePerKg)
            println("net sales = $netSales")

            var factoringChargeAmount = 0f
            var factoringAmountAdvance = 0f

            // if invoice was factored.
//            if (vaultData.confirmPaymentData?.wasFactored == true) {
//                if (vaultData.confirmPaymentData?.factoringCharges != null) {
//                    factoringChargeAmount =
//                        netSales * (vaultData.confirmPaymentData?.factoringCharges!!.toFloat() / 100)
//                }
//            }
            // Factoring Amount Advance = Net sales - Factoring Charge Amount
            factoringAmountAdvance = netSales - factoringChargeAmount


            // Technical Service Provider TSPP = TSP% * net sales

            var tspMargin = vaultData.creationData.tspMargin
            if (tspMargin == null) {
                throw Exception("Technology Service Provider Margin not present.")
            }
            tspMargin /= 100
            // payment to TSP = % configured for TSP * Net Sales (S)

            val paymentToTSP = tspMargin * netSales

            // broker margin * (net sales - TSPP)
            val grossBrokerReceivable = brokerMargin * (netSales - paymentToTSP)
            val netBroker = grossBrokerReceivable + transportationFee
            println("transportation fee = $transportationFee")
            println("net broker = $netBroker")

            // amount transferred to SogeBank = factoring amount advance - broker share - transportation fee
            val amountTransferredToSogeBank =
                factoringAmountAdvance - grossBrokerReceivable - transportationFee - paymentToTSP
            println("amount transferred to sogeBank = $amountTransferredToSogeBank")

            // bank transaction charges = bank transaction fee * amount transaferred to sogebank
            val bankTransactionCharge = Constants.BANK_TRANSACTION_FEE * amountTransferredToSogeBank
            println("bank transaction charge = $bankTransactionCharge")
            val amountAfterBankTransFee = amountTransferredToSogeBank - bankTransactionCharge
            println(amountAfterBankTransFee)

            // payment to LSP

            val serviceRequestId = vaultData.lotsSold.map { it.id }.first()
            println("service request id = $serviceRequestId")
            val lot = QueryVault().queryServiceRequestById(serviceRequestId, Vault.StateStatus.UNCONSUMED, vaultService)
                ?: throw Exception("lot ID $serviceRequestId not found")

            val lotShippingMode = lot.state.data.fruitFlowData?.shippingDetailsData?.shippingDetails
                ?: throw Exception("Shipping details data does not exist")

            // not used
            val ratePerKg = vaultData.creationData.ratePerKg ?: throw Exception("Rate per kg is not present.")

            // lsp gets: lsp % (amount transferred to sogebank - bank transaction charge)
            var lspPercentage: Float? = vaultData.creationData.lspPercentageDistribution
                ?: throw Exception("LSP percentage is not present.")
            if (lspPercentage == null) {
                throw Exception("LSP percentage is not present.")
            }
            lspPercentage /= 100

            val percentageToLSPP = lspPercentage * (amountTransferredToSogeBank - bankTransactionCharge)
            println("Percentage to LSPP $percentageToLSPP")
            val paymentToLsp = percentageToLSPP - totalLSPAdvanceGiven
            println("payment to lsp = $paymentToLsp")

            // payment to extension service provider

            //payment to NGO

            // producers revenue = Amount Transferred to Sogebank (ATS) - Bank Transaction Charges (Y1) -Payment to ESP(ESPP) - Payment to LSP (LSPP)
            val producersRevenue = amountTransferredToSogeBank - bankTransactionCharge - percentageToLSPP
            println("producers revenue = $producersRevenue")

            // sae fee 1% of Net Sales
//            val icgFee = netSales * 0.01f
//            val icga = icgFee + totalAdvanceGiven
//            println("icg fee = $icgFee")

//            val producerDistribution = mutableListOf<ProducerData>()
            val lotData = mutableListOf<LotDistributionModel>()
            producerData.forEach {
                val producersLot = vaultData.lotsSold.filter { lot -> lot.id == it.serviceRequestId }.first()
                val boxesSelectedFromFarmersLot = producersLot.boxesSold
                println("boxes selected from farmers lot = $boxesSelectedFromFarmersLot")
                val ratio = boxesSelectedFromFarmersLot.toFloat() / totalBoxesSold.toFloat()
                println("farmers ratio = $ratio")
//                var amountToProducer = 0f

                val advanceGiven = if (it.advanceGiven == null) 0f else it.advanceGiven
                println("advance given = $advanceGiven")

                val producerAmount = if (advanceGiven != null) {
                    (ratio * producersRevenue) - advanceGiven
                } else {
                    ratio * producersRevenue
                }
                println("producer ${it.farmerName} 's revenue = $producerAmount")
                lotData.add(
                    LotDistributionModel(
                        lotId = it.serviceRequestId,
                        displayId = it.displayId,
                        penalties = it.penalties,
                        currency = Currency.USD,
                        ngoAmount = 0f,
                        ngoName = it.ngo,
                        producerName = it.farmerName,
                        identificationNo = it.identificationNo,
                        phoneNo = it.phoneNo,
                        location = it.location,
                        producerAmount = producerAmount,
                        boxesSold = boxesSelectedFromFarmersLot,
                        paymentMode = it.paymentMode,
                        cashpaymentData = it.cashPaymentData,
                        mobilePaymentData = it.mobilePaymentData,
                        wirePaymentData = it.wirePaymentData,
                        advanceConsidered = it.advanceGiven,
                        exchangeRate = it.exchangeRate
                    )
                )
            }
            val salesData = mutableListOf<SalesDistributionModel>()
            salesData.add(
                SalesDistributionModel(
                    role = Roles.LSP,
                    amount = paymentToLsp,
                    currency = Currency.USD,
                    advanceConsidered = totalLSPAdvanceGiven,
                    organizationName = vaultData.creationData.lspOrganizationName,
                    organizationKey = vaultData.creationData.lspOrganizationKey
                )
            )
            salesData.add(
                SalesDistributionModel(
                    role = Roles.BROKER,
                    amount = netBroker,
                    currency = Currency.USD,
                    organizationName = vaultData.creationData.brokerOrganizationName,
                    organizationKey = vaultData.creationData.brokerOrganizationKey
                )
            )
            salesData.add(
                SalesDistributionModel(
                    role = Roles.TSP,
                    amount = paymentToTSP,
                    currency = Currency.USD,
                    organizationName = vaultData.creationData.tspOrganizationName,
                    organizationKey = vaultData.creationData.tspOrganizationKey
                )
            )

            return PaymentDistributionModel(
                saleId = saleId,
                soldOn = vaultData.creationData.saleTransactionDate,
                netSales = netSales,
                pricePerKg = pricePerKg,
                product = soldFruit,
                totalWeightOfFruitsSold = totalBoxesSold * avgWeight,
                shipmentMode = lotShippingMode,
                totalBoxesSold = totalBoxesSold,
                wasFactored = vaultData.confirmPaymentData?.wasFactored,
                factoringCharges = vaultData.confirmPaymentData?.factoringCharges,
                factoringAmount = factoringChargeAmount,
                factoringEntity = vaultData.confirmPaymentData?.factoringEntity,
                factoringAgent = vaultData.confirmPaymentData?.factoringAgent,
                brokerMargin = brokerMargin,
                brokerTransport = vaultData.shipOrderData?.sellCostOfTransportation,
                bankTransactionCharges = bankTransactionCharge,
                lots = lotData,
                sales = salesData
            )
        }

        fun getDistribution(vaultService: CordaRPCOps): PaymentDistributionModel? {
            val wantedStateRef = QueryVault().querySalesById(saleId, Vault.StateStatus.UNCONSUMED, vaultService)
                ?: throw Exception("Sale ID ${saleId} not found.")

            val vaultData = wantedStateRef.state.data
            return vaultData.paymentDistribution
        }
    }
}