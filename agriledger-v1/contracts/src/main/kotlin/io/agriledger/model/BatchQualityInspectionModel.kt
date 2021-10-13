package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class BatchQualityInspectionModel(val fruitsRejected: Int,
                                  val additionalComments: String
)
