package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class HydroCoolingExitModel(val durationHydroCooling: String? = null,
                            val timeofExitHydroCooling: LocalDateTime? = null,
                            val internalFruitTempHydroCooling: Float? = null
)