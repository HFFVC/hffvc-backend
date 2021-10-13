package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ArrivedAtPackHouseDTO (
        val id: String
        , val arrivedAtPackhouseAddedOn: String
        , val arrivedAtPackhouseAdditionalNote: String?
        , val transportCostArrivedAtPackhouse: String?
        , val timeOfAdmittance: String
        , val status: String
)