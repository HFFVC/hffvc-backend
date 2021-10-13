package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class SamplingDetailsModel(val samplesTaken: Int? = null,
                           val samplingTemperature: Float? = null,
                           val dateAndTimeofSampling: LocalDateTime? = null
)