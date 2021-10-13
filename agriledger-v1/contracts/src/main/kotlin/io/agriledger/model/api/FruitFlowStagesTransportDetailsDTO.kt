package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesTransportDetailsDTO(val currency: String,
                                         val departureDateAndTime: String,
                                         val destination: String,
                                         val mandatory: String,
                                         val temperature: String,
                                         val transportConditions: String,
                                         val transportCost: String
)
