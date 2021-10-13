package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CartonFillingAndPalletizingModel(val startQRCodeBoxes: String? = null,
                                       val endQRCodeBoxes: String? = null,
                                       val startQRCodeFruits: String? = null,
                                       val endQRCodeFruits: String? = null,
                                       val totalBoxes: Int? = null,
                                       val totalFruits: Int? = null,
                                       val boxSize: Int? = null,
                                       val avgWeight: Float? = null
)