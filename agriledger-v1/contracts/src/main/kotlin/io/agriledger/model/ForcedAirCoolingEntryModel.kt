package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ForcedAirCoolingEntryModel(val entryTimeForcedCooling: LocalDateTime? = null
                                 , val fruitTemperatureEntryForcedCooling: Float? = null
                                 , val airflowTemperatureRH: Float? = null
)