package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class PenaltyModel(val type: String,
                   val boxes_amount: Int? = null,
                   val temperature_breach_count: Int? = null,
                   val delivery_time: String? = null,
                   val fruits_amount: Int? = null
)