package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ForcedAirCoolingRemovalDTO(val removalTimeForcedCooling: String?
                                 , val fruitTemperatureRemovalForcedCooling: String?
)