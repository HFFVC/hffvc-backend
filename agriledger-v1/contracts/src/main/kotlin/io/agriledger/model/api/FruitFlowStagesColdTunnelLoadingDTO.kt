package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesColdTunnelLoadingDTO(val destinationDateAndTime: String,
                                          val mandatory: String,
                                          val reeferWallTemperature: String
)