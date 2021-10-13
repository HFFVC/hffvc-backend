package io.agriledger.model.api

import io.agriledger.model.enumerations.UpdateBatchSteps
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UpdateBatchDTO(val batchId: String,
                     val step: UpdateBatchSteps,
                     val arrivalAndDestination: ArrivalAndDestinationDTO?,
                     val qualityInspection: BatchQualityInspectionDTO?,
                     val coldStorage: BatchColdStorageDTO?,
                     val costOfMaturation: CostOfMaturationDTO?
)