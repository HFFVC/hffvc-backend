package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class NoSaleDTO(val batchId: String,
                val status: String
)