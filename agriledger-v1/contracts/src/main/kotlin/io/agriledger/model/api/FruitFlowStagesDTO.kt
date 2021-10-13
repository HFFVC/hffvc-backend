package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowStagesDTO(val cartonFillingAndPalletizing: FruitFlowStagesCartonFillingPalletizingDTO,
                         val cleaningAndTrimming: FruitFlowStagesCleaningAndTrimmingDTO,
                         val coldRoomStorageIn: FruitFlowStagescoldRoomStorageInDTO,
                         val coldRoomStorageOut: FruitFlowStagescoldRoomStorageOutDTO,
                         val coldTunnelLoading: FruitFlowStagesColdTunnelLoadingDTO,
                         val containerInspection: FruitFlowStagesContainerInspectionDTO,
                         val feedPackingLineConveyorBelt: FruitFlowStagesFeedPackingLineConveyorBeltDTO,
                         val forcedAirCoolingEntry: FruitFlowStagesForcedAirCoolingEntryDTO,
                         val fruitWashing: FruitFlowStagesFruitWashingDTO,
                         val grading: FruitFlowStagesGradingDTO,
                         val hotWaterTreatmentEntry: FruitFlowStagesHotWaterTreatmentEntryDTO,
                         val hotWaterTreatmentExit: FruitFlowStagesHotWaterTreatmentExitDTO,
                         val hydroCoolingEntry: FruitFlowStagesHydroCoolingEntryDTO,
                         val hydroCoolingExit: FruitFlowStagesHydroCoolingExitDTO,
                         val larvaeTesting: FruitFlowStagesLarvaeTestingDTO,
                         val preCoolingReefers: FruitFlowStagesPreCoolingReefersDTO,
                         val qualityInspection: FruitFlowStagesQualityInspectionDTO,
                         val removalFromColdStorage: FruitFlowStagesRemovalFromColdStorageDTO,
                         val removalFromForcedAirCooling: FruitFlowStagesRemovalFromForcedAirCoolingDTO,
                         val sampling: FruitFlowStagesSamplingDTO,
                         val secondSizing: FruitFlowStagesSecondSizingDTO,
                         val shippingDetails: FruitFlowStagesShippingDetailsDTO,
                         val sizing: FruitFlowStagesSizingDTO,
                         val temperatureMeasurement: FruitFlowStagesTemperatureMeasurementDTO,
                         val temperatureReadingPackedLot: FruitFlowStagesTemperatureReadingPackedLotDTO,
                         val transferToColdStorage: FruitFlowStagesTransferToColdStorageDTO,
                         val transportDetails: FruitFlowStagesTransportDetailsDTO,
                         val uploadProductCertifications: FruitFlowStagesUploadProductCertificationsDTO
)
