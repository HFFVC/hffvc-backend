package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ArrivedAtPackhouseModel(val arrivedAtPackhouseAddedOn: LocalDateTime
                              , val arrivedAtPackhouseAdditionalNote: String?
                              , val transportCostArrivedAtPackhouse: Float?
                              , val timeOfAdmittance: LocalDateTime
)