package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class SamplingDetailsDTO(val samplesTaken: String?
                         , val samplingTemperature: String?
                         , val dateAndTimeofSampling: String?
)