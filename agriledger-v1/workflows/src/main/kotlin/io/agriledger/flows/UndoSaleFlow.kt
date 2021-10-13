package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.BatchContract
import io.agriledger.contracts.SalesContract
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.LotSaleModel
import io.agriledger.model.NewSaleModel
import io.agriledger.model.api.NewSaleDTO
import io.agriledger.model.api.UndoSaleDTO
import io.agriledger.model.enumerations.Product
import io.agriledger.states.BatchState
import io.agriledger.states.SalesState
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Duration
import java.time.LocalDateTime
import kotlin.Exception

object UndoSaleFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: UndoSaleDTO) : FlowLogic<SignedTransaction>() {
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

            val wantedSalesRef = QueryVault().querySalesById(reqParam.saleId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Sale ${reqParam.saleId} does not exist.")

            val salesVault = wantedSalesRef.state.data

            val updatedSalesList = mutableListOf<LotSaleModel>()
            salesVault.lotsSold.forEach {
                if (it.id != reqParam.lotId) {
                    updatedSalesList.add(it)
                }
            }

            val wantedLotRef = QueryVault().queryServiceRequestById(reqParam.lotId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Service request ${reqParam.lotId} not found.")

            val lotVaultData = wantedLotRef.state.data

            val updatedSaleIds = mutableListOf<LotSaleModel>()
            lotVaultData.saleIds.forEach {
                if (it.id != reqParam.saleId) {
                    updatedSaleIds.add(it)
                }
            }

            progressTracker.currentStep = TX_COMPONENTS

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            var updatedRemainingBoxes: Int? = null
            if (updatedSaleIds.size > 0) {
                var totalBoxes = 0
                lotVaultData.fruitFlowData?.cartonFillingAndPalletizingData?.forEach {
                   lot -> if (lot.totalBoxes != null) totalBoxes += lot.totalBoxes!!
                }

                updatedSaleIds.forEach {
                    totalBoxes = totalBoxes.minus(it.boxesSold)
                }
                    updatedRemainingBoxes = totalBoxes
            }

            var totalBoxesSold = 0
            updatedSalesList.forEach { totalBoxesSold += it.boxesSold }
            val salesOutputState: SalesState = salesVault.copy(
                    lotsSold = updatedSalesList,
                    creationData = NewSaleModel(
                            sellBuyerAddress = salesVault.creationData.sellBuyerAddress,
                            totalNoOfBoxesSold = totalBoxesSold,
                            salesBrokerMargin = salesVault.creationData.salesBrokerMargin,
                            salesCurrency = salesVault.creationData.salesCurrency,
                            salesPricePerKg = salesVault.creationData.salesPricePerKg,
                            salesWeightPerBox = salesVault.creationData.salesWeightPerBox,
                            saleTransactionDate = salesVault.creationData.saleTransactionDate,
                            sellBuyerContactDetails = salesVault.creationData.sellBuyerContactDetails,
                            sellBuyerEmailAddress = salesVault.creationData.sellBuyerEmailAddress,
                            sellBuyerName = salesVault.creationData.sellBuyerName,
                            sellBuyerOrganization = salesVault.creationData.sellBuyerOrganization,
                            soldProduct = salesVault.creationData.soldProduct
                    )
            )
            val serviceRequestOutputState: ServiceRequestState = lotVaultData.copy(
                    creationData = lotVaultData.creationData.copy(status = "PACKING COMPLETED"),
                    saleIds = updatedSaleIds,
                    remainingBoxes = updatedRemainingBoxes
            )

            val serviceRequestCommand = Command(ServiceRequestContract.Commands.UndoSale(), listOf(ourIdentity.owningKey))
            val salesCommand = Command(SalesContract.SalesCommand.UndoSale(), listOf(ourIdentity.owningKey))
            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)

            txBuilder.addInputState(wantedLotRef)
            txBuilder.addInputState(wantedSalesRef)
            txBuilder.addOutputState(serviceRequestOutputState)
            txBuilder.addOutputState(salesOutputState)
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