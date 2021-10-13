package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class PenaltyDistributionModel(val displayId: String?,
                               val penalties: MutableList<PenaltyDataModel> = mutableListOf()
)