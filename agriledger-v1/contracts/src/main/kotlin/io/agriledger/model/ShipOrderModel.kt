package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ShipOrderModel(val dateAndTimeOfLoading: LocalDateTime,
                     val totalNoofBoxesLoaded: Int,
                     val sellCostOfTransportation: Float,
                     val buyersLocation: String,
                     val shipOrderCurrency: String
)