package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class HotWaterTreatmentEntryDTO(val timeofEntryHotWaterTreatment: String?,
                                val waterTemperatureHotWaterTreatment: String?,
                                val phLevelHotWaterTreatment: String?,
                                val chlorineLevelHotWaterTreatment: String?
)