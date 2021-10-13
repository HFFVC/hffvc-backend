package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class HotWaterTreatmentExitDTO(val durationHotWaterTreatment: String?,
                               val timeofExitHotWaterTreatment: String?
)