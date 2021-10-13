package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class NoSaleModel(val batchId: String,
                  val status: String
)