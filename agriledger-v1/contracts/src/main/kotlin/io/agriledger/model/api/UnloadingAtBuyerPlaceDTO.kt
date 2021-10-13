package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UnloadingAtBuyerPlaceDTO(val dateAndTimeOfUnLoading: String,
                               val totalNoofBoxesUnLoaded: String
)