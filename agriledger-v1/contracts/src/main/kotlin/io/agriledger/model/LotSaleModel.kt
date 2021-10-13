package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class LotSaleModel(val id: String,
                   val boxesSold: Int
)