package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ProducerDistributionModel(val farmerName: String,
                                val amount: Float
)