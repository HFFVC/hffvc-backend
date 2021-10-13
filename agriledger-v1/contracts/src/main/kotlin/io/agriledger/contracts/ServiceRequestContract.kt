package io.agriledger.contracts

import io.agriledger.model.enumerations.Product
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.util.regex.Pattern

// ************
// * Contract *
// ************
class ServiceRequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "io.agriledger.contracts.ServiceRequestContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Create -> {
                requireThat {
                    "No inputs should be consumed while a Service Request is created" using (tx.inputs.isEmpty())
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.sae.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by SAE" using (command.signers.containsAll(expectedSigners))
                    output.usernames.forEach { email -> "Email address should be valid" using (isEmailValid(email)) }
                    "Status should be New for a freshly created service request" using (output.creationData.status == "New")
                    val signatoriesOwningKeyList = output.signatories.map { party -> party.owningKey }
                    val signatoriesOwningKeySet = signatoriesOwningKeyList.toHashSet()
                    "Owning key of each signatory should be unique" using (signatoriesOwningKeyList.size == signatoriesOwningKeySet.size)
                }
            }
            is Commands.AcceptServiceRequest -> {
                requireThat {
                    "Only one input should be consumed while accepting service request" using (tx.inputs.size == 1)
                    "Single Accepted Service request state must be generated" using (tx.outputs.size == 1)
                    "There should be two signers" using (command.signers.count() == 2)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey, output.collector.owningKey)
                    "Service request ID must be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP and Collector" using (command.signers.containsAll(expectedSigners))
                    "Input status should be New" using (input.creationData.status == "New")
                    "Output status should be Assigned" using (output.creationData.status == "Assigned")
                    "Values in the input state should not undergo unanticipated changes" using (
                            input.creationData.displayId == output.creationData.displayId &&
                                    input.creationData.cin == output.creationData.cin &&
                                    input.creationData.nif == output.creationData.nif &&
                                    input.creationData.producer == output.creationData.producer &&
                                    input.creationData.farmerId == output.creationData.farmerId &&
                                    input.creationData.product == output.creationData.product &&
                                    input.creationData.requestedOn == output.creationData.requestedOn
                            )
                }
            }
            is Commands.CollectorEnRouteToProducer -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.collector.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by Collector" using (command.signers.containsAll(expectedSigners))
                    "Input status must be Assigned" using (input.creationData.status == "Assigned")
                    "Output status must be Enroute Producer" using (output.creationData.status == "Enroute Producer")
                    "Enroute producer data should not be empty" using (output.enrouteToProducerData != null)
                    "Additional notes, if present, should be less than 200 characters" using (output.enrouteToProducerData?.enrouteAdditionalNote == null ||
                                                                                    output.enrouteToProducerData?.enrouteAdditionalNote?.length!! < 200)

                }
            }
            is Commands.RejectLot -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.collector.owningKey)
                    "Service Request Id should be valid" using (!output.id.isBlank())
                    "Transaction should be signed by Collector" using (command.signers.containsAll(expectedSigners))
                }
            }
            is Commands.WithProducer -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.collector.owningKey)
                    "Service Request Id should be valid" using (!output.id.isBlank())
                    "Transaction should be signed by Collector" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Lot should not be a rejected one" using (output.lotRejectData == null)
                    "Fruit temperature should be less than or equal to 60 degrees celsius" using (output.withProducerData?.temperature!! <= 60)
                    "Fruit temperature should be greater than or equal to -20 degrees celsius" using (output.withProducerData?.temperature!! >=  -20)
                    "Ambient temperature should be less than or equal to 60 degrees celsius" using (output.withProducerData.ambientTemp <= 60)
                    "Ambient temperature should be greater than or equal to -20 degrees celsius" using (output.withProducerData.ambientTemp >=  -20)
                    "At least one crate is needed" using (output.withProducerData.crates > 0)
                    "At least one fruit needs to be harvested" using (output.withProducerData.fruitsHarvested > 0)
                    "Fruits rejected should be zero or more" using (output.withProducerData.fruitRejected >= 0)
                    "Advance given should be zero or more" using (output.withProducerData.advanceGiven >= 0)
                    "Currency should be either USD or HTG" using (output.withProducerData.currency == "HTG" || output.withProducerData.currency == "USD")
                    "Input status must be Enroute Producer" using (input.creationData.status == "Enroute Producer")
                    "Output status must be With Producer" using (output.creationData.status == "With Producer")
                }
            }
            is Commands.CollectorEnrouteToPackhouse -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.collector.owningKey)
                    "Service Request Id should be valid" using (output.id.isNotBlank())
                    "Transaction should be signed by Collector" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    if (output.enrouteToPackhouseData?.enroutePackhouseAdditionalNote != null) {
                        "Additional notes should not be more than 200 characters" using (output.enrouteToPackhouseData.enroutePackhouseAdditionalNote.length <= 200)
                    }
                    "Input status must be With Producer" using (input.creationData.status == "With Producer")
                    "Output status must be Enroute Packhouse" using (output.creationData.status == "Enroute Packhouse")
                    "Parameters in the input state should not undergo unanticipated changes" using (
                            input.creationData.farmerId == output.creationData.farmerId &&
                                    input.creationData.cin == output.creationData.cin &&
                                    input.creationData.nif == output.creationData.nif &&
                                    input.creationData.producer == output.creationData.producer &&
                                    input.creationData.farmerId == output.creationData.farmerId &&
                                    input.creationData.product == output.creationData.product &&
                                    input.creationData.requestedOn == output.creationData.requestedOn &&
                                    input.acceptData?.amount == output.acceptData?.amount &&
                                    input.acceptData?.collectionPoint == output.acceptData?.collectionPoint &&
                                    input.acceptData?.dateOfPickup == output.acceptData?.dateOfPickup &&
                                    input.acceptData?.dropOffAtPackhouse == output.acceptData?.dropOffAtPackhouse &&
                                    input.enrouteToProducerData?.plannedPickUpAddedOn == output.enrouteToProducerData?.plannedPickUpAddedOn &&
                                    input.withProducerData?.currency == output.withProducerData?.currency &&
                                    input.withProducerData?.crates == output.withProducerData?.crates &&
                                    input.withProducerData?.fruitsHarvested == output.withProducerData?.fruitsHarvested &&
                                    input.withProducerData?.ambientTemp == output.withProducerData?.ambientTemp &&
                                    input.withProducerData?.conversionRate == output.withProducerData?.conversionRate &&
                                    input.withProducerData?.lspAdvanceGiven == output.withProducerData?.lspAdvanceGiven &&
                                    input.withProducerData?.lspAdvanceGivenCurrency == output.withProducerData?.lspAdvanceGivenCurrency &&
                                    input.withProducerData?.startingQRCode == output.withProducerData?.startingQRCode &&
                                    input.withProducerData?.endingQRCode == output.withProducerData?.endingQRCode
                            )
                }
            }
            is Commands.CollectorArrivedAtPackHouse -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.collector.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by Collector" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Input status must be Enroute Packhouse" using (input.creationData.status == "Enroute Packhouse")
                    "Output status must be Arrived At Packhouse" using (output.creationData.status == "Arrived At Packhouse")
                }
            }
            is Commands.FruitFlowLarvaeTesting -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowTemperatureMeasurement -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowQualityInspection -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    val qualityInspectionData = output.fruitFlowData?.qualityInspectionData
//                    "Fruits accepted should be greater than zero" using (qualityInspectionData?.fruitsAccepted!! > 0)
//                    "Fruits rejected should be zero or more" using (qualityInspectionData.fruitsRejected >= 0)
//                    val totalFruits = output.withProducerData?.fruitsHarvested
//                    "Total fruits accepted/rejected cannot be more than fruits harvested (WITH_PRODUCER)" using (
//                            (qualityInspectionData?.fruitsAccepted + qualityInspectionData?.fruitsRejected) <= totalFruits!!
//                            )
                }
            }
            is Commands.FruitFlowCleaningAndTrimming -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                    "Cleaning and Trimming is only for Pineapple fruit" using (
                            output.creationData.product == Product.PINEAPPLE
                            )
                }
            }
            is Commands.FruitFlowFruitWashing -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                    "Fruit Washing is not for Avocado Fruit" using (output.creationData.product != Product.AVOCADO)
                }
            }
            is Commands.FruiFlowFeedPackingLineConveyorBelt -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowGrading -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowSecondSizing -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowCartonFillingAndPalletizing -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "At least one box should be generated" using (output.fruitFlowData?.cartonFillingAndPalletizingData?.totalBoxes!! > 0)
//                    "At least one fruit should be there" using (output.fruitFlowData.cartonFillingAndPalletizingData.totalFruits!! > 0)
                }
            }
            is Commands.FruitFlowTemperatureReadingPackedLot -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowForcedAirCoolingEntry -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowForcedAirCoolingRemoval -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "Forced Air Cooling Entry data should exist" using (output.fruitFlowData?.forcedAirCoolingEntryData != null)
                }
            }
            is Commands.FruiFlowColdRoomStorageIn -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowColdRoomStorageOut -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "Cold Room Storage In Data Should Exist" using (output.fruitFlowData?.coldRoomStorageInData != null)
                }
            }
            is Commands.FruitFlowSampling -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
                }
            }
            is Commands.FruitFlowShippingDetails -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "Shipment should either be by SHIPMENT_BY_SEA or SHIPMENT_BY_AIR" using (
//                            output.fruitFlowData?.shippingDetailsData?.shippingDetails == Shipping.SHIPMENT_BY_AIR ||
//                                    output.fruitFlowData?.shippingDetailsData?.shippingDetails == Shipping.SHIPMENT_BY_SEA
//                            )
                }
            }
            is Commands.FruitFlowPreCoolingReefers -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "Fruit Flow Shipping Details should exist" using (output.fruitFlowData?.shippingDetailsData != null)
//                    "Shipment mode has to be SHIPMENT_BY_SEA" using (
//                            output.fruitFlowData?.shippingDetailsData?.shippingDetails == Shipping.SHIPMENT_BY_SEA
//                            )
                }
            }
            is Commands.FruitFlowColdTunnelLoading -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "Fruit Flow Shipping Details should exist" using (output.fruitFlowData?.shippingDetailsData != null)
//                    "Shipment mode has to be SHIPMENT_BY_SEA" using (
//                            output.fruitFlowData?.shippingDetailsData?.shippingDetails == Shipping.SHIPMENT_BY_SEA
//                            )
                }
            }
            is Commands.FruitFlowFirstSizing -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "FIRST_SIZING is only for Mango" using (output.creationData.product == Product.MANGO)
                }
            }
            is Commands.FruitFlowHotWaterTreatmentEntry -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "HOT_WATER_TREATMENT_ENTRY is only for Mango" using (output.creationData.product == Product.MANGO)
                }
            }
            is Commands.FruitFlowHotWaterTreatmentExit -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "HOT_WATER_TREATMENT_EXIT is only for Mango" using (output.creationData.product == Product.MANGO)
//                    "HOT_WATER_TREATMENT_ENTRY data should exist" using (output.fruitFlowData?.hotWaterTreatmentEntryData != null)
                }
            }
            is Commands.FruitFlowHydroCoolingEntry -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "HYDRO_COOLING_ENTRY is only for Mango" using (output.creationData.product == Product.MANGO)
                }
            }
            is Commands.FruitFlowHydroCoolingExit -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "HYDRO_COOLING_EXIT is only for Mango" using (output.creationData.product == Product.MANGO)
//                    "HYDRO_COOLING_ENTRY data should exist" using (output.fruitFlowData?.hydroCoolingEntryData != null)
                }
            }
            is Commands.FruitFlowTransferToColdStorage -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "TRANSFER_TO_COLD_STORAGE is only for Avocado" using (output.creationData.product == Product.AVOCADO)
                }
            }
            is Commands.FruitFlowRemovalFromColdStorage -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)
//                    "REMOVAL_FROM_COLD_STORAGE is only for Avocado" using (output.creationData.product == Product.AVOCADO)
//                    "TRANSFER_TO_COLD_STORAGE data should exist" using (output.fruitFlowData?.transferTocoldStorageData != null)
                }
            }
            is Commands.FruitFlowTransportDetails -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Service Request Id should be valid" using (!output.id.isNullOrBlank())
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                    "Accept Service Request data should exist" using (output.acceptData != null)
                    "Collector - With Producer Data Should exist" using (output.withProducerData != null)
                    "Collector - Enroute to Packhouse data should exist" using (output.enrouteToPackhouseData != null)
                    "Collector - Arrived at packhouse data should exist" using (output.arrivedAtPackhouseData != null)

//                    val fruitFlowData = output.fruitFlowData

//                    "LARVAE_TESTING data should exist" using (fruitFlowData?.larvaeTestingData != null)
//                    "TEMPERATURE_MEASUREMENT data should exist" using (fruitFlowData?.temperatureMeasurementData != null)
//                    "QUALITY_INSPECTION data should exist" using (fruitFlowData?.qualityInspectionData != null)
//                    "FEED_PACKING_LINE_CONVEYOR_BELT data should exist" using (fruitFlowData?.feedPackingLineConveyorData != null)
//                    "GRADING data should exist" using (fruitFlowData?.gradingData != null)
//                    "SECOND_SIZING data should exist" using (fruitFlowData?.secondSizingData != null)
//                    "CARTON_FILLING_AND_PALLETIZING data should exist" using (fruitFlowData?.cartonFillingAndPalletizingData != null)
//                    "TEMPERATURE_READING_FOR_PACKED_LOT data should exist" using (fruitFlowData?.temperatureReadingPackedLotData != null)
//                    "FORCED_AIR_COOLING_ENTRY data should exist" using (fruitFlowData?.forcedAirCoolingEntryData != null)
//                    "FORCED_AIR_COOLING_REMOVAL data should exist" using (fruitFlowData?.forcedAirCoolingRemovalData != null)
//                    "COLD_ROOM_STORAGE_IN data should exist" using (fruitFlowData?.coldRoomStorageInData != null)
//                    "COLD_ROOM_STORAGE_OUT data should exist" using (fruitFlowData?.coldRoomStorageOutData != null)
//                    "SHIPPING_DETAILS data should exist" using (fruitFlowData?.shippingDetailsData != null)
//
//                    // Fruit specific flows
//                    when (output.creationData.product) {
//                        Product.PINEAPPLE -> {
//                            "CLEANING_AND_TRIMMING data should exist" using (fruitFlowData?.cleaningAndTrimmingData != null)
//                            "FRUIT_WASHING data should exist" using (fruitFlowData?.fruitWashingData != null)
//                        }
//                        Product.MANGO -> {
//                            "FRUIT_WASHING data should exist" using (fruitFlowData?.fruitWashingData != null)
//                            "FIRST_SIZING data should exist" using (fruitFlowData?.firstSizingData != null)
//                            "HOT_WATER_TREATMENT_ENTRY data should exist" using (fruitFlowData?.hotWaterTreatmentEntryData != null)
//                            "HOT_WATER_TREATMENT_EXIT data should exist" using (fruitFlowData?.hotWaterTreatmentExitData != null)
//                            "HYDRO_COOLING_ENTRY data should exist" using (fruitFlowData?.hydroCoolingEntryData != null)
//                            "HYDRO_COOLING_EXIT data should exist" using (fruitFlowData?.hydroCoolingExitData != null)
//                        }
//                        Product.AVOCADO -> {
//                            "TRANSFER_TO_COLD_STORAGE data should exist" using (fruitFlowData?.transferTocoldStorageData != null)
//                            "REMOVAL_FROM_COLD_STORAGE data should exist" using (fruitFlowData?.removalFromColdStorageData != null)
//                        }
//                    }
//                    // only if SHIPMENT_BY_SEA
//                    if (output.fruitFlowData?.shippingDetailsData?.shippingDetails == Shipping.SHIPMENT_BY_SEA) {
//                        "SAMPLING_DETAILS data should exist" using (fruitFlowData?.samplingDetailsData != null)
//                        "PRE_COOLING_REEFERS data should exist" using (fruitFlowData?.preCoolingReefersData != null)
//                        "COLD_TUNNEL_LOADING data should exist" using (fruitFlowData?.coldTunnelLoadingData != null)
//                    }
                }
            }
            is Commands.BatchAssociation -> {
                requireThat {
                    "At least one service request state should be provided as input" using (tx.inputsOfType<ServiceRequestState>()
                            .isNotEmpty())
                    "At least one service request should be generated as output" using (tx.outputsOfType<ServiceRequestState>()
                            .isNotEmpty())
                    "There should be a single signer" using (command.signers.count() == 1)

                    val output = tx.outputsOfType<ServiceRequestState>().first()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                }
            }
            is Commands.SaleMappingToLot -> {
                requireThat {
                    "There should be at least one input" using (tx.inputs.isNotEmpty())
                    "There should be at least one output output" using (tx.outputs.isNotEmpty())
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<ServiceRequestState>()
                    output.forEach {
                        val expectedSigners = listOf(it.broker.owningKey)
                        "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                        "Insufficient boxes available for this sale" using (it.remainingBoxes!! >= 0)
                    }
                }
            }

            is Commands.CalculatePenalty -> {
                requireThat {
                    "There should be at least one input" using (tx.inputs.isNotEmpty())
                    "There should be at least one output output" using (tx.outputs.isNotEmpty())
                    val input = tx.inputsOfType<ServiceRequestState>().single()
                    "Penalty should be calculated only once" using (input.penaltiesIncurred.size == 0)
                }
            }
            is Commands.NoSale -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
                "There should be a single signer" using (command.signers.count() == 1)
                val output = tx.outputsOfType<ServiceRequestState>()
                output.forEach {
                    val expectedSigners = listOf(it.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is Commands.UpdateNoSale -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
                "There should be a single signer" using (command.signers.count() == 1)
                val output = tx.outputsOfType<ServiceRequestState>()
                output.forEach {
                    val expectedSigners = listOf(it.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is Commands.UndoSale -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.EditDetails -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.DeletePenalties -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.EditServiceRequestDetails -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.UpdateIdentificationNo -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.UpdateFirstSale -> {
                "There should be at least one input" using (tx.inputs.isNotEmpty())
                "There should be at least one output output" using (tx.outputs.isNotEmpty())
            }
            is Commands.DeleteServiceRequest -> {
                requireThat {
                    "There should be only one input state" using (tx.inputs.size == 1)
                    "There should not be any output state" using (tx.outputStates.isEmpty())
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class AcceptServiceRequest : Commands
        class CollectorEnRouteToProducer : Commands
        class RejectLot : Commands
        class WithProducer : Commands
        class CollectorEnrouteToPackhouse : Commands
        class CollectorArrivedAtPackHouse : Commands
        class FruitFlowLarvaeTesting : Commands
        class FruitFlowTemperatureMeasurement : Commands
        class FruitFlowQualityInspection : Commands
        class FruitFlowCleaningAndTrimming : Commands
        class FruitFlowFruitWashing : Commands
        class FruiFlowFeedPackingLineConveyorBelt : Commands
        class FruitFlowGrading : Commands
        class FruitFlowSecondSizing : Commands
        class FruitFlowCartonFillingAndPalletizing : Commands
        class FruitFlowTemperatureReadingPackedLot : Commands
        class FruitFlowForcedAirCoolingEntry : Commands
        class FruitFlowForcedAirCoolingRemoval : Commands
        class FruiFlowColdRoomStorageIn : Commands
        class FruitFlowColdRoomStorageOut : Commands
        class FruitFlowSampling : Commands
        class FruitFlowShippingDetails : Commands
        class FruitFlowPreCoolingReefers : Commands
        class FruitFlowColdTunnelLoading : Commands
        class FruitFlowFirstSizing : Commands
        class FruitFlowHotWaterTreatmentEntry : Commands
        class FruitFlowHotWaterTreatmentExit : Commands
        class FruitFlowHydroCoolingEntry : Commands
        class FruitFlowHydroCoolingExit : Commands
        class FruitFlowTransferToColdStorage : Commands
        class FruitFlowRemovalFromColdStorage : Commands
        class FruitFlowTransportDetails : Commands
        class BatchAssociation : Commands
        class SaleMappingToLot : Commands
        class TestServiceRequestRecord : Commands
        class CalculatePenalty : Commands
        class NoSale : Commands
        class UpdateNoSale : Commands
        class UndoSale : Commands
        class EditDetails : Commands
        class DeletePenalties : Commands
        class EditServiceRequestDetails : Commands
        class UpdateIdentificationNo : Commands
        class UpdateFirstSale : Commands
        class DeleteServiceRequest : Commands
    }

    private fun isEmailValid(email: String): Boolean {
        return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }
}