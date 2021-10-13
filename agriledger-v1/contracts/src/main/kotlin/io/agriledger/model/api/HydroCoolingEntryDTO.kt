package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class HydroCoolingEntryDTO(val timeofEntryHydroCooling: String?,
                           val waterTemperatureHydroCooling: String?,
                           val phLevelHydroCooling: String?,
                           val chlorineLevelHydroCooling: String?
)