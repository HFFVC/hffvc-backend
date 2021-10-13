package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CostOfMaturationModel(val costOfMaturation: Float,
                            val currency: String
)
