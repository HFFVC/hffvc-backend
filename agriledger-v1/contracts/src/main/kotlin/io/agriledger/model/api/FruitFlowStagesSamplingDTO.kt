package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesSamplingDTO(val mandatory: String,
                                 val samplesTaken: String,
                                 val temperature: String,
                                 val timestamp: String
)
