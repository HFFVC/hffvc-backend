package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TransportDetailsDTO(val departureDateTimeTransport: String?,
                          val transportTemperature: String?,
                          val destinationTransport: String?,
                          val transportConditions: String?,
                          val transportCost: String?,
                          val transportCurrency: String?,
                          val status: String?,
                          val fruitFlowCompletedOn: String?,
                          val penalityAmount: String?,
                          val lossOfBox: String?
)