package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitWashingModel(val waterTemperature: Float? = null
                        , val phLevel: Float? = null
                        , val chlorineLevel: Float? = null
)