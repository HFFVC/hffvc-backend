package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesHotWaterTreatmentExitDTO(val durationOfTreatment : String,
                                              val mandatory : String,
                                              val timeOfExit : String)
