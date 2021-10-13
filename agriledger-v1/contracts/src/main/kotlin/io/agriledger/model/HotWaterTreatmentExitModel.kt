package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class HotWaterTreatmentExitModel(val durationHotWaterTreatment: String? = null,
                                 val timeofExitHotWaterTreatment: LocalDateTime? = null
)