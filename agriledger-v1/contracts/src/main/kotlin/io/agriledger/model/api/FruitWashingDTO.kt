package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitWashingDTO(val waterTemperature: String?
                      , val phLevel: String?
                      , val chlorineLevel: String?
)