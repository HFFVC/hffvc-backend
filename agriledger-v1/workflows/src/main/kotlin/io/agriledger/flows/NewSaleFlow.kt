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

object NewSaleFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: NewSaleDTO, val email: String?) : FlowLogic<SignedTransaction>() {
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

            // checking if saleId already exists.
            val saleIdExists =
                QueryVault().querySalesById(reqParam.saleId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            if (saleIdExists != null) {
                throw Exception("sale Id ${reqParam.saleId} already exists.")
            }

            // checking if the batch has exceeded 3 weeks time period
            val wantedBatchRef =
                QueryVault().queryBatchById(reqParam.batchId, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Batch ${reqParam.batchId} not found.")
            val vaultData = wantedBatchRef.state.data

            val currentDateTime = LocalDateTime.parse(reqParam.saleTransactionDate)
            val batchCreatedOn = vaultData.batchUpdateData?.arrivalAndDestinationData?.arrivalTimestamp
                ?: throw Exception("Batch Arrival and Destination data doesn't exist.")

            val differenceInDays = Duration.between(batchCreatedOn, currentDateTime).toDays()
            if (differenceInDays > 21) {
                val updatedServiceRequests = subFlow(UpdateNoSaleStatusFlow.Initiator(reqParam.batchId, email))
                throw Exception("Selected batch has exceeded 3 weeks time period.")
            }
            val newSale = vaultData.saleIds.toMutableList()
            newSale.add(reqParam.saleId)
            val batchOutputState: BatchState = vaultData.copy(saleIds = newSale)

            val criteria: QueryCriteria.VaultQueryCriteria =
                QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

            val allWantedStateRefs = reqParam.lots.map { lot ->
                QueryVault().queryServiceRequestById(lot.id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
            }
            allWantedStateRefs.forEach {
                if (it == null) {
                    throw Exception("Service request id not found")
                }
            }
            val allWantedStates = allWantedStateRefs.map { it?.state?.data }

            if (allWantedStates.first() != null) {
                if (allWantedStates.first()!!.broker != ourIdentity) {
                    throw IllegalArgumentException("Could not proceed with flow execution. The party is not authorised to run this flow.")
                }
            }

            val allServiceRequestOutputStates = mutableListOf<ServiceRequestState>()
            allWantedStates.forEach {
                val newList = it?.saleIds?.toMutableList()

                // subtracting boxes sold from total number of boxes
                var cartonFilingAndPalletizingBoxes = 0

                it?.fruitFlowData?.cartonFillingAndPalletizingData?.forEach { lot ->
                    if (lot.totalBoxes != null) {
                        cartonFilingAndPalletizingBoxes += lot.totalBoxes!!
                    }
                }
                var boxesAvailableForSale =
                    if (it?.remainingBoxes != null) it.remainingBoxes else cartonFilingAndPalletizingBoxes


                val lotBoxes = reqParam.lots.first { lot -> lot.id == it?.id }
                boxesAvailableForSale = boxesAvailableForSale?.minus(lotBoxes.boxesSold)
                newList?.add(LotSaleModel(id = reqParam.saleId, boxesSold = lotBoxes.boxesSold))


                if (it != null) {
                    if (boxesAvailableForSale == 0) {
                        allServiceRequestOutputStates.add(
                            it.copy(
                                saleIds = newList!!,
                                remainingBoxes = boxesAvailableForSale,
                                creationData = it.creationData.copy(
                                    status = "SOLD_OUT"
                                )
                            )
                        )
                    } else {
                        allServiceRequestOutputStates.add(
                            it.copy(
                                saleIds = newList!!,
                                remainingBoxes = boxesAvailableForSale
                            )
                        )
                    }
                }
            }


            progressTracker.currentStep = TX_COMPONENTS

            val sae: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.saeName)
                ?: throw IllegalArgumentException("Couldn't find counterparty for SAE in identity service")

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val salesOutputState = SalesState(
                saleId = reqParam.saleId,
                batchId = reqParam.batchId,
                lsp = lsp,
                sae = sae,
                collector = collector,
                broker = ourIdentity,
                creationData = NewSaleModel(
                    sellBuyerName = reqParam.sellBuyerName,
                    sellBuyerAddress = reqParam.sellBuyerAddress,
                    sellBuyerOrganization = reqParam.sellBuyerOrganization,
                    sellBuyerContactDetails = reqParam.sellBuyerContactDetails,
                    sellBuyerEmailAddress = reqParam.sellBuyerEmailAddress,
                    saleTransactionDate = LocalDateTime.parse(reqParam.saleTransactionDate),
                    totalNoOfBoxesSold = reqParam.totalNoOfBoxesSold.toInt(),
                    soldProduct = Product.valueOf(reqParam.soldProduct.toUpperCase()),
                    salesPricePerKg = reqParam.salesPricePerKg.toFloat(),
                    salesWeightPerBox = reqParam.salesWeightPerBox.toFloat(),
                    salesCurrency = reqParam.salesCurrency,
                    salesBrokerMargin = reqParam.salesBrokerMargin.toFloat(),
                    lspOrganizationName = reqParam.lspOrganizationName,
                    lspOrganizationKey = reqParam.lspOrganizationKey,
                    lspPercentageDistribution = reqParam.lspPercentageDistribution.toFloat(),
                    brokerOrganizationName = reqParam.brokerOrganizationName,
                    brokerOrganizationKey = reqParam.brokerOrganizationKey,
                    avgWeight = reqParam.avgWeight.toFloat(),
                    ratePerKg = reqParam.ratePerKg.toFloat(),
                    tspMargin = reqParam.tspMargin.toFloat(),
                    tspOrganizationKey = reqParam.tspOrganizationKey,
                    tspOrganizationName = reqParam.tspOrganizationName
                ),
                lotsSold = reqParam.lots,
                signatories = mutableListOf(ourIdentity),
                usernames = mutableSetOf(email!!)
            )

            val serviceRequestCommand =
                Command(ServiceRequestContract.Commands.SaleMappingToLot(), listOf(ourIdentity.owningKey))
            val batchCommand = Command(BatchContract.BatchCommands.SaleAssociation(), listOf(ourIdentity.owningKey))
            val salesCommand = Command(SalesContract.SalesCommand.CreateSale(), listOf(ourIdentity.owningKey))
            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)

            allWantedStateRefs.map {
                if (it != null) {
                    txBuilder.addInputState(it)
                }
            }
            txBuilder.addInputState(wantedBatchRef)
            allServiceRequestOutputStates.map { txBuilder.addOutputState(it, ServiceRequestContract.ID) }
            txBuilder.addOutputState(batchOutputState, BatchContract.ID)
            txBuilder.addOutputState(salesOutputState, SalesContract.ID)
            txBuilder.addCommand(serviceRequestCommand)
            txBuilder.addCommand(salesCommand)
            txBuilder.addCommand(batchCommand)

            // Signing the transaction.
            progressTracker.currentStep = TX_SIGNING
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Verifying the transaction.
            progressTracker.currentStep = TX_VERIFICATION
            txBuilder.verify(serviceHub)

            // We finalise the transaction and then send it to the counterparty.
            progressTracker.currentStep = FINALISATION

            val saeSession = initiateFlow(sae)
            val collectorSession = initiateFlow(collector)
            val lspSession = initiateFlow(lsp)

            return subFlow(FinalityFlow(signedTx, listOf(saeSession, collectorSession, lspSession)))
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