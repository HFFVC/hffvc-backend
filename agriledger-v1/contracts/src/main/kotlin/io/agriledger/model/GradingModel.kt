package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class GradingModel(val gradingResults: String? = null
                   , val weightofRemovedFruit: Float? = null
)