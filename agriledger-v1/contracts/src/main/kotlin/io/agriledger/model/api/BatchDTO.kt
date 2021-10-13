package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class BatchDTO(val batchId: String,
               val batchCreatedAt: String,
               val lotIds: MutableList<String>,
               val palletStartQRCode: String,
               val palletEndQRCode: String
)