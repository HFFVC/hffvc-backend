package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class FruitFlowModel(val larvaeTestingData: LarvaeTestingModel? = null,
                          val temperatureMeasurementData: TemperatureMeasurementModel? = null,
                          val qualityInspectionData: QualityInspectionModel? = null,
                          val cleaningAndTrimmingData: CleaningAndTrimmingModel? = null,
                          val fruitWashingData: FruitWashingModel? = null,
                          val feedPackingLineConveyorData: FeedPackingLineConveyorBeltModel? = null,
                          val gradingData: GradingModel? = null,
                          val secondSizingData: SecondSizingModel? = null,
                          val cartonFillingAndPalletizingData: MutableList<CartonFillingAndPalletizingModel>? = null,
                          val temperatureReadingPackedLotData: TemperatureReadingPackedLotModel? = null,
                          val forcedAirCoolingEntryData: ForcedAirCoolingEntryModel? = null,
                          val forcedAirCoolingRemovalData: ForcedAirCoolingRemovalModel? = null,
                          val coldRoomStorageInData: ColdRoomStorageInModel? = null,
                          val coldRoomStorageOutData: ColdRoomStorageOutModel? = null,
                          val shippingDetailsData: ShippingDetailsModel? = null,
                          val samplingDetailsData: SamplingDetailsModel? = null,
                          val preCoolingReefersData: PreCoolingReefersModel? = null,
                          val coldTunnelLoadingData: ColdTunnelLoadingModel? = null,
                          val firstSizingData: FirstSizingModel? = null,
                          val hotWaterTreatmentEntryData: HotWaterTreatmentEntryModel? = null,
                          val hotWaterTreatmentExitData: HotWaterTreatmentExitModel? = null,
                          val hydroCoolingEntryData: HydroCoolingEntryModel? = null,
                          val hydroCoolingExitData: HydroCoolingExitModel? = null,
                          val transferTocoldStorageData: TransferToColdStorageModel? = null,
                          val removalFromColdStorageData: RemovalFromColdStorageModel? = null,
                          val transportDetailsData: TransportDetailsModel? = null
)