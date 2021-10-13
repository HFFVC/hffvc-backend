package io.agriledger.model.api

import io.agriledger.model.PenaltyModel
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EnrouteToPackhouseDTO(val id: String
                            , val status: String
                            , val enroutePackhouseAddedOn: String
                            , val enroutePackhouseAdditionalNote: String?
                            , val dropOffAtPackhouse: String

)