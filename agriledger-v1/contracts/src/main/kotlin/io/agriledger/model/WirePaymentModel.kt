package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class WirePaymentModel(
    val SelectedBank: String,
    val selectedBankCode: String,
    val AccountNumber: String,
    val BankCurrency: String,
    val accountType: String? = null
)