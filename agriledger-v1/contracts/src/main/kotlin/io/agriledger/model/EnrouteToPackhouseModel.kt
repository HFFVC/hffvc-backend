package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class EnrouteToPackhouseModel(val enroutePackhouseAddedOn: LocalDateTime
                              , val enroutePackhouseAdditionalNote: String?
                              , val dropOffAtPackhouse: String
)