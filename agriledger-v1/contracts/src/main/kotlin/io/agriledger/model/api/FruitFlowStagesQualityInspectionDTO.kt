package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesQualityInspectionDTO(val fruitsAccepted: String,
                                          val fruitsRejected: String,
                                          val mandatory: String,
                                          val qualityInspectionResults: String
)
