package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class HydroCoolingExitDTO(val durationHydroCooling: String?,
                          val timeofExitHydroCooling: String?,
                          val internalFruitTempHydroCooling: String?
)