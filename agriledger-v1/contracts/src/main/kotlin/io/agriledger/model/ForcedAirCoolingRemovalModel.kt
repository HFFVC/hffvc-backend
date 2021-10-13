package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ForcedAirCoolingRemovalModel(val removalTimeForcedCooling: LocalDateTime? = null
                                   , val fruitTemperatureRemovalForcedCooling: Float? = null
)