package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class GradingDTO(val gradingResults: String?
                 , val weightofRemovedFruit: String?
)