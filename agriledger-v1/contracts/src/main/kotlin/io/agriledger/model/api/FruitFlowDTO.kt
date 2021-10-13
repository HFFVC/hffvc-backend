package io.agriledger.model.api

import io.agriledger.model.enumerations.FruitFlowStep
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FruitFlowDTO(val id: String,
                   val step: FruitFlowStep,
                   val larvaeTesting: LarvaeTestingDTO?,
                   val temperatureMeasurement: TemperatureMeasurementDTO?,
                   val qualityInspection: QualityInspectionDTO?,
                   val cleaningAndTrimming: CleaningAndTrimmingDTO?,
                   val fruitWashing: FruitWashingDTO?,
                   val feedLineConveyorBelt: FeedPackingLineConveyorBeltDTO?,
                   val grading: GradingDTO?,
                   val secondSizing: SecondSizingDTO?,
                   val cartonFillingAndPalletizing: MutableList<CartonFillingAndPalletizingDTO>?,
                   val temperatureReadingPackedLot: TemperatureReadingPackedLotDTO?,
                   val forcedAirCoolingEntry: ForcedAirCoolingEntryDTO?,
                   val forcedAirCoolingRemoval: ForcedAirCoolingRemovalDTO?,
                   val coldRoomStorageIn: ColdRoomStorageInDTO?,
                   val coldRoomStorageOut: ColdRoomStorageOutDTO?,
                   val shippingDetails: ShippingDetailsDTO?,
                   val samplingDetails: SamplingDetailsDTO?,
                   val preCoolingReefers: PreCoolingRefersDTO?,
                   val coldTunnelLoading: ColdTunnelLoadingDTO?,
                   val firstSizing: FirstSizingDTO?,
                   val hotWaterTreatmentEntry: HotWaterTreatmentEntryDTO?,
                   val hotWaterTreatmentExit: HotWaterTreatmentExitDTO?,
                   val hydroCoolingEntry: HydroCoolingEntryDTO?,
                   val hydroCoolingExit: HydroCoolingExitDTO?,
                   val transferToColdStorage: TransferToColdStorageDTO?,
                   val removalFromColdStorage: RemovalFromColdStorageDTO?,
                   val transportDetails: TransportDetailsDTO?,
                   val fruitFlowStages: FruitFlowStagesDTO
)