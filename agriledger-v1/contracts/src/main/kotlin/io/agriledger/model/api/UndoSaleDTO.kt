package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UndoSaleDTO(val saleId: String,
                  val lotId: String
)