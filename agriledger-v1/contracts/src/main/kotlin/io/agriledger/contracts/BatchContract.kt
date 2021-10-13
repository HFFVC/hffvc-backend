package io.agriledger.contracts

import io.agriledger.states.BatchState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class BatchContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "io.agriledger.contracts.BatchContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.

        val command = tx.commands.requireSingleCommand<BatchCommands>()
        when (command.value) {
            is BatchCommands.CreateBatch -> {
                requireThat {
                    "The output should be a single batch" using (tx.outputsOfType<BatchState>().size == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey)
                    "Pallet start QR Code cannot be empty" using (output.palletStartQRCode.isNotEmpty())
                    "Pallet end QR Code cannot be empty" using (output.palletEndQRCode.isNotEmpty())
                    "There should be a single signer" using (command.signers.count() == 1)
                    "Transaction should be signed by LSP" using (command.signers.containsAll(expectedSigners))
                }
            }
            is BatchCommands.ProformaInvoiceCreation -> {
                requireThat {
                    "A single batch should be accepted as input" using (tx.inputs.size == 1)
                    "A single batch should be generated as output" using (tx.outputs.size == 1)
                    "There should be two signer" using (command.signers.count() == 2)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.lsp.owningKey, output.broker.owningKey)
                    "Batch Id should be valid" using (!output.batchId.isNullOrBlank())
                    "Transaction should be signed by LSP and Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is BatchCommands.BatchArrivalAndDestination -> {
                requireThat {
                    "A single Batch should be accepted as input" using (tx.inputs.size == 1)
                    "A single Batch should be generated as output" using (tx.outputs.size == 1)
                    "There should be only one signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is BatchCommands.BatchColdStorage -> {
                requireThat {
                    "A single Batch should be accepted as input" using (tx.inputs.size == 1)
                    "A single Batch should be generated as output" using (tx.outputs.size == 1)
                    "There should be only one signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                    "Arrival and destination data should be present" using (output.batchUpdateData?.arrivalAndDestinationData != null)
                }
            }
            is BatchCommands.CostOfMaturation -> {
                requireThat {
                    "A single Batch should be accepted as input" using (tx.inputs.size == 1)
                    "A single Batch should be generated as output" using (tx.outputs.size == 1)
                    "There should be only one signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                    "Arrival and destination data should be present" using (output.batchUpdateData?.arrivalAndDestinationData != null)
                }
            }
            is BatchCommands.BatchQualityInspection -> {
                requireThat {
                    "A single Service Request should be accepted as input" using (tx.inputs.size == 1)
                    "A single Service Request should be generated as output" using (tx.outputs.size == 1)
                    "There should be only one signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is BatchCommands.SaleAssociation -> {
                requireThat {
                    "At least one batch should be provided as input" using (tx.inputsOfType<BatchState>().isNotEmpty())
                    "At least one batch should be generated as output" using (tx.outputsOfType<BatchState>().isNotEmpty())
                    "There should be a single signer" using (command.signers.count() == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                    "At least one sale id should be present" using (output.saleIds.isNotEmpty())
                }
            }
            is BatchCommands.TestBatchRecord -> {
                requireThat {
                    "The output should be a single batch" using (tx.outputsOfType<BatchState>().size == 1)
                    val output = tx.outputsOfType<BatchState>().single()
                    "Pallet start QR Code cannot be empty" using (output.palletStartQRCode.isNotEmpty())
                    "Pallet end QR Code cannot be empty" using (output.palletEndQRCode.isNotEmpty())
                    "There should be a single signer" using (command.signers.count() == 1)
                }
            }
            is BatchCommands.DeleteDuplicateBatch -> {
                requireThat {
                    "There should be only one Batch input state" using (tx.inputs.size == 1)
                    "There should not be any output state" using (tx.outputStates.isEmpty())
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface BatchCommands : CommandData {
        class CreateBatch : BatchCommands
        class ProformaInvoiceCreation : BatchCommands
        class BatchArrivalAndDestination : BatchCommands
        class BatchQualityInspection : BatchCommands
        class BatchColdStorage : BatchCommands
        class CostOfMaturation : BatchCommands
        class SaleAssociation : BatchCommands
        class TestBatchRecord : BatchCommands
        class DeleteDuplicateBatch : BatchCommands
    }
}