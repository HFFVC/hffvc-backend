package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesHotWaterTreatmentEntryDTO( val PHLevel : String,
                                                val chlorineLevel : String,
                                                val mandatory : String,
                                                val timeOfEntry : String,
                                                val waterTemperature : String
)
