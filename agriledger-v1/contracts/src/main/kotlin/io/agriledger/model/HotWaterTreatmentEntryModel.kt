package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class HotWaterTreatmentEntryModel(val timeofEntryHotWaterTreatment: LocalDateTime? = null,
                                  val waterTemperatureHotWaterTreatment: Float? = null,
                                  val phLevelHotWaterTreatment: Float? = null,
                                  val chlorineLevelHotWaterTreatment: Float? = null
)