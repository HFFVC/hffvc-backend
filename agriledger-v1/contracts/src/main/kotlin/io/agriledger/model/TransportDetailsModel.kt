package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class TransportDetailsModel(val departureDateTimeTransport: LocalDateTime? = null,
                            val transportTemperature: Float? = null,
                            val destinationTransport: String? = null,
                            val transportConditions: String? = null,
                            val transportCost: Float? = null,
                            val transportCurrency: String? = null,
                            val fruitFlowCompletedOn: LocalDateTime? = null,
                            val penalityAmount: Float? = null,
                            val lossOfBox: Int? = null
)