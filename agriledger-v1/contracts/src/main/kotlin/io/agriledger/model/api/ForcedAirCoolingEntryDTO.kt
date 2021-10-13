package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ForcedAirCoolingEntryDTO(val entryTimeForcedCooling: String?
                               , val fruitTemperatureEntryForcedCooling: String?
                               , val airflowTemperatureRH: String?
)