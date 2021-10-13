package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CostOfMaturationDTO(val costOfMaturation: String,
                          val currency: String
)