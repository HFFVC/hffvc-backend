package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class BatchQualityInspectionDTO(val fruitsRejected: String,
                                val additionalComments: String
)