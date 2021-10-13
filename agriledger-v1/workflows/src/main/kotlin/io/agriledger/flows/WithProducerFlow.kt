package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.flows.utils.FlowConstants
import io.agriledger.flows.utils.NotarySelector
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.*
import io.agriledger.model.api.WithProducerDTO
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.intellij.lang.annotations.Flow
import java.lang.Exception
import java.time.LocalDateTime

object WithProducerFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: WithProducerDTO, val email: String?) : FlowLogic<SignedTransaction>() {
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
            if (!validateFlowConstraints(reqParam)) {
                throw FlowException("Could not initiate the transaction. One or more flow constraints were not met.")
            }
            // Initiator flow logic goes here.

            // We retrieve the notary identity from the network map.

            val notary = NotarySelector().getNotary(serviceHub)

            // We create the transaction components.

            progressTracker.currentStep = EXTRACTING_VAULT_STATES

            val wantedStateRef = QueryVault().queryServiceRequestById(reqParam.id, Vault.StateStatus.UNCONSUMED, serviceHub.vaultService)
                    ?: throw Exception("Service Request ID does not exist.")

            val vaultData = wantedStateRef.state.data

            if (ourIdentity != vaultData.collector) {
                throw IllegalArgumentException("Could not proceed with flow execution. The party is not authorised to run this flow.")
            }

            progressTracker.currentStep = TX_COMPONENTS

            val newSignatories = vaultData.signatories.toMutableList()
            newSignatories.add(ourIdentity)

            val usenames = vaultData.usernames.toMutableSet()
            usenames.add(email!!)

            val outputState: ServiceRequestState = vaultData.copy(
                    withProducerData = WithProducerModel(
                            temperature = reqParam.temperature.toFloat(),
                            ambientTemp = reqParam.ambientTemp.toFloat(),
                            crates = reqParam.crates.toFloat(),
                            fruitsHarvested = reqParam.fruitsHarvested.toInt(),
                            fruitRejected = reqParam.fruitRejected.toInt(),
                            advanceGiven = reqParam.advanceGiven.toFloat(),
                            currency = reqParam.currency,
                            startingQRCode = reqParam.startingQRCode,
                            endingQRCode = reqParam.endingQRCode,
                            paymentMethod = reqParam.paymentMethod,
                            ngo = reqParam.ngo,
                            lspAdvanceGivenCurrency = reqParam.lspAdvanceGivenCurrency,
                            lspAdvanceGiven = if (reqParam.lspAdvanceGiven != null) {
                                reqParam.lspAdvanceGiven?.toFloat()
                            } else {
                                null
                            },
                            cashPaymentData = reqParam.cashPayment?.let {
                                CashPaymentModel(
                                        cashPaymentCurrency = it.cashPaymentCurrency,
                                        farmerVoucher = it.farmerVoucher,
                                        contactnumber = it.contactnumber
                                )
                            },
                            mobilePaymentData = reqParam.mobilePayment?.let {
                                MobilePaymentModel(
                                        mobilePaymentOperator = it.mobilePaymentOperator,
                                        mobilePaymentCurrency = it.mobilePaymentCurrency,
                                        mobilePaymentNumber = it.mobilePaymentNumber
                                )
                            },
                            wirePaymentData = reqParam.wirePayment?.let {
                                WirePaymentModel(
                                        SelectedBank = it.SelectedBank,
                                        AccountNumber = it.AccountNumber,
                                        BankCurrency = it.BankCurrency,
                                        selectedBankCode = it.selectedBankCode,
                                        accountType = it.accountType
                                )
                            },
                            AdditionalNotes = reqParam.AdditionalNotes,
                            withProducerQrCodeFile = reqParam.withProducerQrCodeFile.toMutableList(),
                            withProducerTimeStamp = LocalDateTime.parse(reqParam.withProducerTimeStamp),
                            conversionCurrency = reqParam.conversionCurrency,
                            conversionRate = reqParam.conversionRate?.toFloat()
                    ),
                    creationData = vaultData.creationData.copy(status = reqParam.status),
                    signatories = newSignatories,
                    usernames = usenames
            )

            val command = Command(ServiceRequestContract.Commands.WithProducer(), listOf(ourIdentity.owningKey))

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

            val lsp: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.lspName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for LSP in identity service")

            val broker: Party = serviceHub.identityService.wellKnownPartyFromX500Name(FlowConstants.brokerName)
                    ?: throw IllegalArgumentException("Couldn't find counterparty for Broker in identity service")

            val saeSession = initiateFlow(sae)
            val lspSession = initiateFlow(lsp)
            val brokerSession = initiateFlow(broker)

            return subFlow(FinalityFlow(signedTx, listOf(saeSession, lspSession, brokerSession)))
        }

        private fun validateFlowConstraints(reqParam: WithProducerDTO): Boolean {
            if (
                    reqParam.withProducerTimeStamp.isBlank() ||
                    reqParam.advanceGiven.isBlank() ||
                    reqParam.advanceGiven.toFloat() < 0 ||
                    reqParam.ambientTemp.isBlank() ||
                    reqParam.crates.isBlank() ||
                    reqParam.crates.toFloat() <= 0 ||
                    reqParam.startingQRCode.isBlank() ||
                    reqParam.endingQRCode.isBlank() ||
                    reqParam.fruitsHarvested.isBlank() ||
                    reqParam.fruitRejected.isBlank() ||
                    (reqParam.conversionRate != null && reqParam.conversionRate!!.isBlank()) ||
                    (reqParam.conversionCurrency != null && reqParam.conversionCurrency!!.isBlank()) ||
                    (reqParam.AdditionalNotes != null && reqParam.AdditionalNotes!!.isBlank()) ||
                    (reqParam.cashPayment != null && (
                            reqParam.cashPayment?.cashPaymentCurrency!!.isBlank() ||
                                    reqParam.cashPayment?.contactnumber!!.isBlank() ||
                                    reqParam.cashPayment?.farmerVoucher!!.isBlank()
                            )) ||
                    (reqParam.wirePayment != null && (
                            reqParam.wirePayment?.AccountNumber!!.isBlank() ||
                                    reqParam.wirePayment?.BankCurrency!!.isBlank() ||
                                    reqParam.wirePayment?.SelectedBank!!.isBlank()
                            )) ||
                    (reqParam.mobilePayment != null && (
                            reqParam.mobilePayment?.mobilePaymentCurrency!!.isBlank() ||
                                    reqParam.mobilePayment?.mobilePaymentOperator!!.isBlank() ||
                                    reqParam.mobilePayment?.mobilePaymentNumber!!.isBlank()
                            ))
            ) {
                return false
            }
            return true
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