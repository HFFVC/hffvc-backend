package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class QualityInspectionDTO(val fruitsAccepted: String?
                           , val fruitsRejected: String?
                           , val qualityInspectionResults: String?
)