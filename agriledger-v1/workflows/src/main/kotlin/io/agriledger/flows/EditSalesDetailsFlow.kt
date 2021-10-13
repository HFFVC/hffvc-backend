package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.SalesContract
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.*
import io.agriledger.model.api.EditSalesDetailsDTO
import io.agriledger.states.SalesState
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception

object EditSalesDetailsFlow {
    // *********
    // * Flows *
    // *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: EditSalesDetailsDTO) : FlowLogic<SignedTransaction>() {
        companion object {
            object EXTRACTING_VAULT_STATES : ProgressTracker.Step("Extracting states from the vault.")
            object TX_COMPONENTS : ProgressTracker.Step("Gathering a transaction's components.")
            object TX_BUILDING : ProgressTracker.Step("Building a transaction.")
            object TX_SIGNING : ProgressTracker.Step("Signing a transaction.")
            object TX_VERIFICATION : ProgressTracker.Step("Verifying a transaction.")
            object FINALISATION : ProgressTracker.Step("Finalising a transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    EXTRACTING_VAULT_STATES,
                    TX_COMPONENTS,
                    TX_BUILDING,
                    TX_SIGNING,
                    TX_VERIFICATION,
                    FINALISATION
            )
        }

        override val progressTracker: ProgressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)

            // We create the transaction components.

            progressTracker.currentStep = EXTRACTING_VAULT_STATES

            val wantedSalesStateRef = QueryVault().querySalesById(reqParam.saleId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Sale ${reqParam.saleId} not found.")

            val salesVaultData = wantedSalesStateRef.state.data

            val updatedLotsSold = mutableListOf<LotSaleModel>()
            updatedLotsSold.add(LotSaleModel(id = reqParam.lotId, boxesSold = reqParam.totalNoOfBoxesSold.toInt()))

            val salesOutputState: SalesState = salesVaultData.copy(
                    creationData = NewSaleModel(
                            sellBuyerName = salesVaultData.creationData.sellBuyerName,
                            sellBuyerAddress = salesVaultData.creationData.sellBuyerAddress,
                            sellBuyerOrganization = salesVaultData.creationData.sellBuyerOrganization,
                            sellBuyerContactDetails = salesVaultData.creationData.sellBuyerContactDetails,
                            sellBuyerEmailAddress = salesVaultData.creationData.sellBuyerEmailAddress,
                            saleTransactionDate = salesVaultData.creationData.saleTransactionDate,
                            totalNoOfBoxesSold = reqParam.totalNoOfBoxesSold.toInt(),
                            soldProduct = salesVaultData.creationData.soldProduct,
                            salesPricePerKg = reqParam.salesPricePerKg.toFloat(),
                            salesWeightPerBox = salesVaultData.creationData.salesWeightPerBox,
                            salesCurrency = salesVaultData.creationData.salesCurrency,
                            salesBrokerMargin = reqParam.salesBrokerMargin.toFloat()
                    ),
                    lotsSold = updatedLotsSold,
                    salesInvoiceData = salesVaultData.salesInvoiceData?.let {
                        SalesInvoiceModel(
                                invoiceNo = it.invoiceNo,
                                billingDate = it.billingDate,
                                brokerName = it.brokerName,
                                brokerOrganization = it.brokerOrganization,
                                buyerName = it.buyerName,
                                buyerOrganization = it.buyerOrganization,
                                noofBoxesPurchased = it.noofBoxesPurchased,
                                product = it.product,
                                pricePerKg = it.pricePerKg,
                                pricePerKgCurrency = it.pricePerKgCurrency,
                                GICurrency = it.GICurrency,
                                netSales = it.netSales,
                                aproximateWeightOfProduct = it.aproximateWeightOfProduct,
                                brokerPercent = it.brokerPercent,
                                brokerTransportFlatFee = reqParam.brokerTransportFlatFee.toFloat()
                        )
                    }
            )

            val wantedLotStateRef = QueryVault().queryServiceRequestById(reqParam.lotId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Lot ${reqParam.lotId} not found.")

            val lotVaultData = wantedLotStateRef.state.data
            val updatedSaleIds = mutableListOf<LotSaleModel>()
            updatedSaleIds.add(LotSaleModel(id = reqParam.saleId, boxesSold = reqParam.totalNoOfBoxesSold.toInt()))
            var totalBoxes = 0
            lotVaultData.fruitFlowData?.cartonFillingAndPalletizingData?.forEach {
                if (it.totalBoxes != null) {
                    totalBoxes += it.totalBoxes!!
                }
            }
            val remainingBoxes = totalBoxes - reqParam.totalNoOfBoxesSold.toInt()

//          lotVaultData.fruitFlowData?.cartonFillingAndPalletizingData?.totalBoxes?.minus(reqParam.totalNoOfBoxesSold.toInt())

            val lotOutputState: ServiceRequestState = lotVaultData.copy(
                    saleIds = updatedSaleIds,
                    remainingBoxes = remainingBoxes
            )

            progressTracker.currentStep = TX_COMPONENTS

            val serviceRequestCommand = Command(ServiceRequestContract.Commands.EditDetails(), listOf(ourIdentity.owningKey))
            val salesCommand = Command(SalesContract.SalesCommand.EditDetails(), listOf(ourIdentity.owningKey))

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
            txBuilder.addInputState(wantedLotStateRef)
            txBuilder.addInputState(wantedSalesStateRef)
            txBuilder.addOutputState(lotOutputState, ServiceRequestContract.ID)
            txBuilder.addOutputState(salesOutputState, SalesContract.ID)
            txBuilder.addCommand(serviceRequestCommand)
            txBuilder.addCommand(salesCommand)

            // Signing the transaction.
            progressTracker.currentStep = TX_SIGNING
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Verifying the transaction.
            progressTracker.currentStep = TX_VERIFICATION
            txBuilder.verify(serviceHub)

            // We finalise the transaction and then send it to the counterparty.
            progressTracker.currentStep = FINALISATION

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val otherParties = listOf<Party>(lsp, sae, collector, broker).filter { it.name != ourIdentity.name }
            val partySessions = mutableListOf<FlowSession>()
            otherParties.forEach {
                val session = initiateFlow(it)
                partySessions.add(session)
            }

            return subFlow(FinalityFlow(signedTx, partySessions))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            // Responder flow logic goes here.
            subFlow(ReceiveFinalityFlow(counterpartySession))
        }
    }
}