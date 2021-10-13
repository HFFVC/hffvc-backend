package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ShipOrderDTO(val dateAndTimeOfLoading: String,
                   val totalNoofBoxesLoaded: String,
                   val sellCostOfTransportation: String,
                   val buyersLocation: String,
                   val shipOrderCurrency: String
)