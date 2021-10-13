package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class TransferToColdStorageDTO(val temperatureTransfer: String?,
                               val coldStorageTimeIn: String?
)