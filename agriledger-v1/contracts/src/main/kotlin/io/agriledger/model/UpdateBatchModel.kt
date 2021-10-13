package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class UpdateBatchModel(val arrivalAndDestinationData: ArrivalAndDestinationModel? = null,
                            val qualityInspectionData: BatchQualityInspectionModel? = null,
                            val coldStorageData: BatchColdStorageModel? = null,
                            val costOfMaturationData: CostOfMaturationModel? = null
)