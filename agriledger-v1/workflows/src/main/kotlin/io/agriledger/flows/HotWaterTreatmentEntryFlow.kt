package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.flows.utils.ValidateFruitFlowStages
import io.agriledger.model.*
import io.agriledger.model.api.FruitFlowDTO
import io.agriledger.model.api.HotWaterTreatmentEntryDTO
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception
import java.time.LocalDateTime

object HotWaterTreatmentEntryFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val id: String, val reqParam: HotWaterTreatmentEntryDTO?, val email: String?, val fruitFlow: FruitFlowDTO) : FlowLogic<SignedTransaction>() {
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

            val wantedStateRef = QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
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

            val outputState: ServiceRequestState = vaultData.copy(
                    fruitFlowData = if (vaultData.fruitFlowData == null) {
                        FruitFlowModel(
                                hotWaterTreatmentEntryData = reqParam?.let {
                                    HotWaterTreatmentEntryModel(
                                            timeofEntryHotWaterTreatment = if (reqParam.timeofEntryHotWaterTreatment != null) {
                                                LocalDateTime.parse(reqParam.timeofEntryHotWaterTreatment)
                                            } else {
                                                null
                                            },
                                            waterTemperatureHotWaterTreatment = if (reqParam.waterTemperatureHotWaterTreatment != null) {
                                                reqParam.waterTemperatureHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            },
                                            chlorineLevelHotWaterTreatment = if (reqParam.chlorineLevelHotWaterTreatment != null) {
                                                reqParam.chlorineLevelHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            },
                                            phLevelHotWaterTreatment = if (reqParam.phLevelHotWaterTreatment != null) {
                                                reqParam.phLevelHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            }
                                    )
                                }
                        )
                    } else {
                        vaultData.fruitFlowData?.copy(
                                hotWaterTreatmentEntryData = reqParam?.let {
                                    HotWaterTreatmentEntryModel(
                                            timeofEntryHotWaterTreatment = if (reqParam.timeofEntryHotWaterTreatment != null) {
                                                LocalDateTime.parse(reqParam.timeofEntryHotWaterTreatment)
                                            } else {
                                                null
                                            },
                                            waterTemperatureHotWaterTreatment = if (reqParam.waterTemperatureHotWaterTreatment != null) {
                                                reqParam.waterTemperatureHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            },
                                            chlorineLevelHotWaterTreatment = if (reqParam.chlorineLevelHotWaterTreatment != null) {
                                                reqParam.chlorineLevelHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            },
                                            phLevelHotWaterTreatment = if (reqParam.phLevelHotWaterTreatment != null) {
                                                reqParam.phLevelHotWaterTreatment?.toFloat()
                                            } else {
                                                null
                                            }
                                    )
                                }
                        )
                    },
                    signatories = newSignatories,
                    usernames = usernames
            )

            val command = Command(ServiceRequestContract.Commands.FruitFlowHotWaterTreatmentEntry(), listOf(ourIdentity.owningKey))

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