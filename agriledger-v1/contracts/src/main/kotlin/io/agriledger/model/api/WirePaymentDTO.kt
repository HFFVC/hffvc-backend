package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class WirePaymentDTO(
    val SelectedBank: String,
    val selectedBankCode: String,
    val AccountNumber: String,
    val BankCurrency: String,
    val accountType: String
)