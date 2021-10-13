package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class HydroCoolingEntryModel(val timeofEntryHydroCooling: LocalDateTime? = null,
                             val waterTemperatureHydroCooling: Float? = null,
                             val phLevelHydroCooling: Float? = null,
                             val chlorineLevelHydroCooling: Float? = null
)