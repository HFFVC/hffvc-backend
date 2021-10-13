package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesCartonFillingPalletizingDTO(
        val endQRCodeBox: String,
        val endQRCodeFruits: String,
        val mandatory: String,
        val startQRCodeBox: String,
        val startQRCodeFruits: String
)