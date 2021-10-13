package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class BatchColdStorageDTO(val temperature: String,
                          val phLevel: String,
                          val ethyleneLevel: String,
                          val co2Level: String,
                          val coldStorageInTimestamp: String
)