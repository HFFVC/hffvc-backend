package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class QualityInspectionModel(val fruitsAccepted: Int? = null
                             , val fruitsRejected: Int? = null
                             , val qualityInspectionResults: String? = null
)