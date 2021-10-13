package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EnrouteToProducerDTO(val id: String
                           , val plannedPickUpAddedOn: String
                           , val enrouteAdditionalNote: String
                           , val status: String
)