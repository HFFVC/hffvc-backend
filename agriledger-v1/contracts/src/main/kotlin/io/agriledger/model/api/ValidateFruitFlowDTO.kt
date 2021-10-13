package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ValidateFruitFlowDTO(val id: String, val fruitFlow: FruitFlowStagesDTO)