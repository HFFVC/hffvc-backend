package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.flows.utils.ValidateFruitFlowStages
import io.agriledger.model.*
import io.agriledger.model.api.*
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import kotlin.Exception

object CartonFillingAndPalletizingFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
        val id: String,
        val reqParam: MutableList<CartonFillingAndPalletizingDTO>?,
        val email: String?,
        val fruitFlow: FruitFlowDTO
    ) : FlowLogic<SignedTransaction>() {
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

            val wantedStateRef =
                QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Service Request ID does not exist.")
            val vaultData = wantedStateRef.state.data
            if (ourIdentity != vaultData.lsp) {
                throw IllegalArgumentException("Could not proceed with flow execution. The party is not authorised to run this flow.")
            }

            if (!ValidateFruitFlowStages().validate(vaultData, fruitFlow)) {
                throw Exception("Mandatory fields/stages cannot be empty")
            }


            progressTracker.currentStep = TX_COMPONENTS

            val newSignatories = vaultData.signatories.toMutableList()
            newSignatories.add(ourIdentity)

            val usernames = vaultData.usernames.toMutableSet()
            usernames.add(email!!)

            // calculating total boxes
            if (reqParam == null) {
                throw Exception("Invalid parameters.")
            }
            val boxesList = mutableListOf<CartonFillingAndPalletizingModel>()

            reqParam.forEach {
                var totalFruits = 1
                var totalBoxes = 1
                var boxSize = 0
                var splittingFactor = 0
                if (it.startQRCodeFruits == null || it.endQRCodeFruits == null) {
                    throw Exception("starting and ending fruit QR codes are needed.")
                }
                if (it.startQRCodeBoxes == null || it.endQRCodeBoxes == null) {
                    throw Exception("Starting and ending box QR codes are needed.")
                }
                if (it.startQRCodeFruits != it.endQRCodeFruits) {
                    for (i in 0 until it.startQRCodeFruits?.length!!) {
                        if (it.startQRCodeFruits!![i] == it.endQRCodeFruits!![i]) {
                            splittingFactor++
                        } else {
                            break
                        }
                    }
                    val startingCount =
                        it.startQRCodeFruits!!.substring(splittingFactor, it.startQRCodeFruits!!.length).toInt()
                    val endingCount =
                        it.endQRCodeFruits!!.substring(splittingFactor, it.endQRCodeFruits!!.length).toInt()

                    totalFruits = (endingCount - startingCount) + 1
                }

                splittingFactor = 0

                if (it.startQRCodeBoxes != it.endQRCodeBoxes) {
                    for (i in 0 until it.startQRCodeBoxes?.length!!) {
                        if (it.startQRCodeBoxes!![i] == it.endQRCodeBoxes!![i]) {
                            splittingFactor++
                        } else {
                            break
                        }
                    }
                    val startingCount =
                        it.startQRCodeBoxes!!.substring(splittingFactor, it.startQRCodeBoxes!!.length).toInt()
                    val endingCount = it.endQRCodeBoxes!!.substring(splittingFactor, it.endQRCodeBoxes!!.length).toInt()

                    totalBoxes = (endingCount - startingCount) + 1
                }
                boxSize = totalFruits / totalBoxes

                boxesList.add(
                    CartonFillingAndPalletizingModel(
                        startQRCodeFruits = it.startQRCodeFruits,
                        endQRCodeFruits = it.endQRCodeFruits,
                        startQRCodeBoxes = it.startQRCodeBoxes,
                        endQRCodeBoxes = it.endQRCodeBoxes,
                        boxSize = boxSize,
                        totalBoxes = totalBoxes,
                        totalFruits = totalFruits
                    )
                )
            }


//            var splittingFactor = 0
//            var totalBoxes = 1
//            if (reqParam.startQRCodeBoxes == null || reqParam.endQRCodeBoxes == null) {
//                throw Exception("Start and End QR Codes are needed.")
//            }
//            if (reqParam.startQRCodeBoxes != reqParam.endQRCodeBoxes) {
//                for (i in 0 until reqParam.startQRCodeBoxes?.length!!) {
//                    if (reqParam.startQRCodeBoxes!![i] == reqParam.endQRCodeBoxes!![i]) {
//                        splittingFactor++
//                    } else {
//                        break
//                    }
//                }
//                val startingQRCode = reqParam.startQRCodeBoxes?.substring(splittingFactor, reqParam.startQRCodeBoxes!!.length)?.toInt()
//                val endingQRCode = reqParam.endQRCodeBoxes?.substring(splittingFactor, reqParam.endQRCodeBoxes!!.length)?.toInt()
//
//                if (endingQRCode != null) {
//                    totalBoxes = endingQRCode - startingQRCode!! + 1
//                }
//
//            }
//
//            // calculating fruits
//            splittingFactor = 0
//            var totalFruits = 1
//            if (reqParam.startQRCodeFruits == null || reqParam.endQRCodeFruits == null) {
//                throw Exception("start and end qr codes are needed.")
//            }
//            if (reqParam.startQRCodeFruits != reqParam.endQRCodeFruits) {
//                for (i in 0 until reqParam.startQRCodeFruits!!.length) {
//                    if (reqParam.startQRCodeFruits!![i] == reqParam.endQRCodeFruits!![i]) {
//                        splittingFactor++
//                    } else {
//                        break
//                    }
//                }
//                val startingFruitQRCode = reqParam.startQRCodeFruits?.substring(splittingFactor, reqParam.startQRCodeFruits!!.length)?.toInt()
//                val endingFruitQRCode = reqParam.endQRCodeFruits?.substring(splittingFactor, reqParam.endQRCodeFruits!!.length)?.toInt()
//
//                if (endingFruitQRCode != null) {
//                    totalFruits = endingFruitQRCode - startingFruitQRCode!! + 1
//                }
//
//            }

            val outputState: ServiceRequestState = vaultData.copy(
                fruitFlowData = if (vaultData.fruitFlowData == null) {
                    FruitFlowModel(
                        cartonFillingAndPalletizingData = boxesList
//                                reqParam.let {
//                                    CartonFillingAndPalletizingModel(
//                                            startQRCodeBoxes = reqParam.startQRCodeBoxes,
//                                            endQRCodeBoxes = reqParam.endQRCodeBoxes,
//                                            startQRCodeFruits = reqParam.startQRCodeFruits,
//                                            endQRCodeFruits = reqParam.endQRCodeFruits,
//                                            totalBoxes = totalBoxes,
//                                            totalFruits = totalFruits
//                                    )
//                                }
                    )
                } else {
                    vaultData.fruitFlowData?.copy(
                        cartonFillingAndPalletizingData = boxesList
//                                reqParam.let {
//                                    CartonFillingAndPalletizingModel(
//                                            startQRCodeBoxes = reqParam.startQRCodeBoxes,
//                                            endQRCodeBoxes = reqParam.endQRCodeBoxes,
//                                            startQRCodeFruits = reqParam.startQRCodeFruits,
//                                            endQRCodeFruits = reqParam.endQRCodeFruits,
//                                            totalBoxes = totalBoxes,
//                                            totalFruits = totalFruits
//                                    )
//                                }
                    )
                },
                signatories = newSignatories,
                usernames = usernames
            )

            val command = Command(
                ServiceRequestContract.Commands.FruitFlowCartonFillingAndPalletizing(),
                listOf(ourIdentity.owningKey)
            )

            // We create a transaction builder and add the components.
            progressTracker.currentStep = TX_BUILDING
            val txBuilder = TransactionBuilder(notary = notary)
                .addInputState(wantedStateRef)
                .addOutputState(outputState, ServiceRequestContract.ID)
                .addCommand(command)


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

            val collector: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.collectorName)
                ?: throw IllegalArgumentException("Couldn't find counterparty for Collector in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val saeSession = initiateFlow(sae)
            val collectorSession = initiateFlow(collector)
            val brokerSession = initiateFlow(broker)

            return subFlow(FinalityFlow(signedTx, listOf(saeSession, collectorSession, brokerSession)))
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