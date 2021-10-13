package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ArrivalAndDestinationDTO(val arrivalTimestamp: String,
                               val destination: String
)