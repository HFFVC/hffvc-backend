package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CartonFillingAndPalletizingDTO(val startQRCodeBoxes: String?,
                                     val endQRCodeBoxes: String?,
                                     val startQRCodeFruits: String?,
                                     val endQRCodeFruits: String?
)