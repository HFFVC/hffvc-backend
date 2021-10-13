package io.agriledger.contracts

import io.agriledger.states.SalesState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class SalesContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "io.agriledger.contracts.SalesContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.

        val command = tx.commands.requireSingleCommand<SalesCommand>()
        when (command.value) {
            is SalesCommand.CreateSale -> {
                requireThat {
                    "Only one sale should be created" using (tx.outputsOfType<SalesState>().count() == 1)

                    val output = tx.outputsOfType<SalesState>().single()
                    val expectedSigners = listOf(output.broker.owningKey)

                    "Batch id should not be empty" using (output.batchId.isNotEmpty())
                    "Sale id should not be empty" using (output.saleId.isNotEmpty())
                    "At least one lot must be selected for creating a sale" using (output.lotsSold.size > 0)
                    "At least one box should be sold" using (output.creationData.totalNoOfBoxesSold > 0)
                    "There should be a single signer" using (command.signers.count() == 1)
                    "Transaction should be signed by Broker" using (command.signers.containsAll(expectedSigners))
                }
            }
            is SalesCommand.ShipOrder -> {
                requireThat {
                    "Only Broker should sign the transaction" using (command.signers.count() == 1)
                }
            }
            is SalesCommand.UnloadingAtBuyer -> {
                requireThat {
                    val output = tx.outputsOfType<SalesState>().single()
                    "Only Broker should sign the transaction" using (command.signers.count() == 1)
                    "The order shipping data should be available" using (output.shipOrderData != null)
                    "Number of boxes loaded and unloaded should be same" using (
                            output.unloadingAtBuyerData?.totalNoofBoxesUnLoaded!! <= output.shipOrderData?.totalNoofBoxesLoaded!!
                            )
                }
            }
            is SalesCommand.SalesInvoice -> {
                requireThat {
                    val output = tx.outputsOfType<SalesState>().single()
                    "Only Broker should sign the transaction" using (command.signers.count() == 1)
                    "The order shipping data should be available" using (output.shipOrderData != null)
                    "Unloading at buyer's place data should be available" using (output.unloadingAtBuyerData != null)
                    "Number of boxes loaded and unloaded should be same" using (
                            output.unloadingAtBuyerData?.totalNoofBoxesUnLoaded!! <= output.shipOrderData?.totalNoofBoxesLoaded!!
                            )
                }
            }
            is SalesCommand.ConfirmPayment -> {
                requireThat {
                    // no output state produced
                    val input = tx.inputsOfType<SalesState>().single()
                    "Only Broker should sign the transaction" using (command.signers.count() == 1)
                    "The order shipping data should be available" using (input.shipOrderData != null)
                    "Invoice data should exist" using (input.salesInvoiceData != null)
                }
            }
            is SalesCommand.UndoSale -> {
                requireThat {
                    "There should be at least one input state" using (tx.inputs.isNotEmpty())
                    "There should be at least one output state" using (tx.outputs.isNotEmpty())
                }
            }
            is SalesCommand.EditDetails -> {
                requireThat {
                    "There should be at least one input state" using (tx.inputs.isNotEmpty())
                    "There should be at least one output state" using (tx.outputs.isNotEmpty())
                }
            }
            is SalesCommand.PaymentDistribution -> {
                requireThat {
                    "There should be at least one input state" using (tx.inputs.isNotEmpty())
                    "There should be at least one output state" using (tx.outputs.isNotEmpty())
                    val input = tx.inputsOfType<SalesState>().single()
                    "Payment Distribution can be computed only once" using (input.paymentDistribution == null)
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface SalesCommand : CommandData {
        class CreateSale : SalesCommand
        class ShipOrder : SalesCommand
        class UnloadingAtBuyer : SalesCommand
        class SalesInvoice : SalesCommand
        class ConfirmPayment : SalesCommand
        class UndoSale : SalesCommand
        class EditDetails : SalesCommand
        class PaymentDistribution : SalesCommand
    }
}