package io.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import io.agriledger.model.api.FruitFlowDTO
import io.agriledger.model.api.FruitFlowStagesDTO
import io.agriledger.model.enumerations.FruitFlowStep
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import java.lang.Exception

object FruitFlow {
    // *********
// * Flows *
// *********
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val reqParam: FruitFlowDTO, val email: String?) : FlowLogic<SignedTransaction>() {


        @Suspendable
        override fun call(): SignedTransaction {
            if (!validateFlowConstraints(reqParam)) {
                throw FlowException("Could not initiate transaction. One or more fields have invalid data.")
            }
            when (reqParam.step) {
                FruitFlowStep.LARVAE_TESTING -> {
                    return subFlow(LarvaeTestingFlow.Initiator(reqParam.id, reqParam.larvaeTesting, email, reqParam))
                }
                FruitFlowStep.TEMPERATURE_MEASUREMENT -> {
                    return subFlow(TemperatureMeasurementFlow.Initiator(reqParam.id, reqParam.temperatureMeasurement, email, reqParam))
                }
                FruitFlowStep.QUALITY_INSPECTION -> {
                    return subFlow(QualityInspectionFlow.Initiator(reqParam.id, reqParam.qualityInspection, email, reqParam))
                }
                FruitFlowStep.CLEANING_AND_TRIMMING -> {
                    return subFlow(CleaningAndTrimmingFlow.Initiator(reqParam.id, reqParam.cleaningAndTrimming, email, reqParam))
                }
                FruitFlowStep.FRUIT_WASHING -> {
                    return subFlow(FruitWashingFlow.Initiator(reqParam.id, reqParam.fruitWashing, email, reqParam))
                }
                FruitFlowStep.FEED_PACKING_LINE_CONVEYOR_BELT -> {
                    return subFlow(FeedPackingLineConveyorBeltFlow.Initiator(reqParam.id, reqParam.feedLineConveyorBelt, email, reqParam))
                }
                FruitFlowStep.GRADING -> {
                    return subFlow(GradingFlow.Initiator(reqParam.id, reqParam.grading, email, reqParam))
                }
                FruitFlowStep.SECOND_SIZING -> {
                    return subFlow(SecondSizingFlow.Initiator(reqParam.id, reqParam.secondSizing, email, reqParam))
                }
                FruitFlowStep.CARTON_FILLING_AND_PALLETIZING -> {
                    return subFlow(CartonFillingAndPalletizingFlow.Initiator(reqParam.id, reqParam.cartonFillingAndPalletizing, email, reqParam))
                }
                FruitFlowStep.TEMPERATURE_READING_PACKED_LOT -> {
                    return subFlow(TemperatureReadingPackedLotFlow.Initiator(reqParam.id, reqParam.temperatureReadingPackedLot, email, reqParam))
                }
                FruitFlowStep.FORCED_AIR_COOLING_ENTRY -> {
                    return subFlow(ForcedAirCoolingEntryFlow.Initiator(reqParam.id, reqParam.forcedAirCoolingEntry, email, reqParam))
                }
                FruitFlowStep.FORCED_AIR_COOLING_REMOVAL -> {
                    return subFlow(ForcedAirCoolingRemovalFlow.Initiator(reqParam.id, reqParam.forcedAirCoolingRemoval, email, reqParam))
                }
                FruitFlowStep.COLD_ROOM_STORAGE_IN -> {
                    return subFlow(ColdRoomStorageInFlow.Initiator(reqParam.id, reqParam.coldRoomStorageIn, email, reqParam))
                }
                FruitFlowStep.COLD_ROOM_STORAGE_OUT -> {
                    return subFlow(ColdRoomStorageOutFlow.Initiator(reqParam.id, reqParam.coldRoomStorageOut, email, reqParam))
                }
                FruitFlowStep.SAMPLING_DETAILS -> {
                    return subFlow(SamplingDetailsFlow.Initiator(reqParam.id, reqParam.samplingDetails, email, reqParam))
                }
                FruitFlowStep.SHIPPING_DETAILS -> {
                    return subFlow(ShippingDetailsFlow.Initiator(reqParam.id, reqParam.shippingDetails, email, reqParam))
                }
                FruitFlowStep.PRE_COOLING_REEFERS -> {
                    return subFlow(PreCoolingReefersFlow.Initiator(reqParam.id, reqParam.preCoolingReefers, email, reqParam))
                }
                FruitFlowStep.COLD_TUNNEL_LOADING -> {
                    return subFlow(ColdTunnelLoadingFlow.Initiator(reqParam.id, reqParam.coldTunnelLoading, email, reqParam))
                }
                FruitFlowStep.FIRST_SIZING -> {
                    return subFlow(FirstSizingFlow.Initiator(reqParam.id, reqParam.firstSizing, email, reqParam))
                }
                FruitFlowStep.HOT_WATER_TREATMENT_ENTRY -> {
                    return subFlow(HotWaterTreatmentEntryFlow.Initiator(reqParam.id, reqParam.hotWaterTreatmentEntry, email, reqParam))
                }
                FruitFlowStep.HOT_WATER_TREATMENT_EXIT -> {
                    return subFlow(HotWaterTreatmentExitFlow.Initiator(reqParam.id, reqParam.hotWaterTreatmentExit, email, reqParam))
                }
                FruitFlowStep.HYDRO_COOLING_ENTRY -> {
                    return subFlow(HydroCoolingEntryFlow.Initiator(reqParam.id, reqParam.hydroCoolingEntry, email, reqParam))
                }
                FruitFlowStep.HYDRO_COOLING_EXIT -> {
                    return subFlow(HydroCoolingExitFlow.Initiator(reqParam.id, reqParam.hydroCoolingExit, email, reqParam))
                }
                FruitFlowStep.TRANSFER_TO_COLD_STORAGE -> {
                    return subFlow(TransferToColdStorageflow.Initiator(reqParam.id, reqParam.transferToColdStorage, email, reqParam))
                }
                FruitFlowStep.REMOVAL_FROM_COLD_STORAGE -> {
                    return subFlow(RemovalFromColdStorageFlow.Initiator(reqParam.id, reqParam.removalFromColdStorage, email, reqParam))
                }
                FruitFlowStep.TRANSPORT_DETAILS -> {
                    return subFlow(TransportDetailsFlow.Initiator(reqParam.id, reqParam.transportDetails, email, reqParam))
                }
                else -> {
                    throw Exception("Invalid step in fruit flow!")
                }
            }
        }

        private fun validateFlowConstraints(reqParam: FruitFlowDTO): Boolean {
            when (reqParam.step) {
                FruitFlowStep.LARVAE_TESTING -> {
                    if ((reqParam.larvaeTesting?.larvaeTesting != null && reqParam.larvaeTesting?.larvaeTesting!!.isBlank()) ||
                            (reqParam.larvaeTesting?.rejectReason != null && reqParam.larvaeTesting?.rejectReason!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.TEMPERATURE_MEASUREMENT -> {
                    if (
                            (reqParam.temperatureMeasurement?.ambientTemp != null && reqParam.temperatureMeasurement?.ambientTemp!!.isBlank()) ||
                            (reqParam.temperatureMeasurement?.internalFruitTemp != null && reqParam.temperatureMeasurement?.internalFruitTemp!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.QUALITY_INSPECTION -> {
                    if (
                            (reqParam.qualityInspection?.qualityInspectionResults != null && reqParam.qualityInspection?.qualityInspectionResults!!.isBlank()) ||
                            (reqParam.qualityInspection?.fruitsAccepted != null && reqParam.qualityInspection?.fruitsAccepted!!.isBlank()) ||
                            (reqParam.qualityInspection?.fruitsRejected != null && reqParam.qualityInspection?.fruitsRejected!!.isBlank())

                    ) {
                        return false
                    }
                }
                FruitFlowStep.CLEANING_AND_TRIMMING -> {
                    if (reqParam.cleaningAndTrimming?.dateTimeCleaningTrimming != null && reqParam.cleaningAndTrimming?.dateTimeCleaningTrimming!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.FRUIT_WASHING -> {
                    if (
                            (reqParam.fruitWashing?.chlorineLevel != null && reqParam.fruitWashing?.chlorineLevel!!.isBlank()) ||
                            (reqParam.fruitWashing?.waterTemperature != null && reqParam.fruitWashing?.waterTemperature!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.FEED_PACKING_LINE_CONVEYOR_BELT -> {
                    if (reqParam.feedLineConveyorBelt?.packingLineTimeofEntry != null && reqParam.feedLineConveyorBelt?.packingLineTimeofEntry!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.GRADING -> {
                    if (
                            (reqParam.grading?.gradingResults != null && reqParam.grading?.gradingResults!!.isBlank()) ||
                            (reqParam.grading?.weightofRemovedFruit != null && reqParam.grading?.weightofRemovedFruit!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.SECOND_SIZING -> {
                    if (reqParam.secondSizing?.secondSizingResults != null && reqParam.secondSizing?.secondSizingResults!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.CARTON_FILLING_AND_PALLETIZING -> {
                    if (reqParam.cartonFillingAndPalletizing != null) {
                        reqParam.cartonFillingAndPalletizing?.forEach {
                            if (
                                    (it.endQRCodeBoxes != null && it.endQRCodeBoxes!!.isBlank()) ||
                                    (it.endQRCodeFruits != null && it.endQRCodeFruits!!.isBlank()) ||
                                    (it.startQRCodeBoxes != null && it.startQRCodeBoxes!!.isBlank()) ||
                                    (it.startQRCodeFruits != null && it.startQRCodeFruits!!.isBlank())
                            ) {
                                return false
                            }
                        }
                    } else {
                        return false
                    }
                }
                FruitFlowStep.TEMPERATURE_READING_PACKED_LOT -> {
                    if (
                            (reqParam.temperatureReadingPackedLot?.ambienttemperaturePacked != null && reqParam.temperatureReadingPackedLot?.ambienttemperaturePacked!!.isBlank()) ||
                            (reqParam.temperatureReadingPackedLot?.internalFruitTemperaturePacked != null && reqParam.temperatureReadingPackedLot?.internalFruitTemperaturePacked!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.FORCED_AIR_COOLING_ENTRY -> {
                    if (
                            (reqParam.forcedAirCoolingEntry?.airflowTemperatureRH != null && reqParam.forcedAirCoolingEntry?.airflowTemperatureRH!!.isBlank()) ||
                            (reqParam.forcedAirCoolingEntry?.entryTimeForcedCooling != null && reqParam.forcedAirCoolingEntry?.entryTimeForcedCooling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.FORCED_AIR_COOLING_REMOVAL -> {
                    if (
                            (reqParam.forcedAirCoolingRemoval?.fruitTemperatureRemovalForcedCooling != null && reqParam.forcedAirCoolingRemoval?.fruitTemperatureRemovalForcedCooling!!.isBlank()) ||
                            (reqParam.forcedAirCoolingRemoval?.removalTimeForcedCooling != null && reqParam.forcedAirCoolingRemoval?.removalTimeForcedCooling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.COLD_ROOM_STORAGE_IN -> {
                    if (
                            (reqParam.coldRoomStorageIn?.airflowTemperatureRHColdStorage != null && reqParam.coldRoomStorageIn?.airflowTemperatureRHColdStorage!!.isBlank()) ||
                            (reqParam.coldRoomStorageIn?.fruitTemperatureColdStorage != null && reqParam.coldRoomStorageIn?.fruitTemperatureColdStorage!!.isBlank()) ||
                            (reqParam.coldRoomStorageIn?.storageTimeIn != null && reqParam.coldRoomStorageIn?.storageTimeIn!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.COLD_ROOM_STORAGE_OUT -> {
                    if (
                            (reqParam.coldRoomStorageOut?.fruitTemperatureColdStorageOut != null && reqParam.coldRoomStorageOut?.fruitTemperatureColdStorageOut!!.isBlank()) ||
                            (reqParam.coldRoomStorageOut?.storageTimeOut != null && reqParam.coldRoomStorageOut?.fruitTemperatureColdStorageOut!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.SAMPLING_DETAILS -> {
                    if (
                            (reqParam.samplingDetails?.dateAndTimeofSampling != null && reqParam.samplingDetails?.dateAndTimeofSampling!!.isBlank()) ||
                            (reqParam.samplingDetails?.samplesTaken != null && reqParam.samplingDetails?.samplesTaken!!.isBlank()) ||
                            (reqParam.samplingDetails?.samplingTemperature != null && reqParam.samplingDetails?.dateAndTimeofSampling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.SHIPPING_DETAILS -> {
                    if (reqParam.shippingDetails?.shippingDetails != null && reqParam.shippingDetails?.shippingDetails!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.PRE_COOLING_REEFERS -> {
                    if (reqParam.preCoolingReefers?.reeferWallTemperature != null && reqParam.preCoolingReefers?.reeferWallTemperature!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.COLD_TUNNEL_LOADING -> {
                    if (
                            (reqParam.coldTunnelLoading?.destinationDateTimeColdTunnelLoading != null && reqParam.coldTunnelLoading?.destinationDateTimeColdTunnelLoading!!.isBlank()) ||
                            (reqParam.coldTunnelLoading?.reeferWallTemperatureColdTunnelLoading != null && reqParam.coldTunnelLoading?.destinationDateTimeColdTunnelLoading!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.FIRST_SIZING -> {
                    if (reqParam.firstSizing?.firstSizingResults != null && reqParam.firstSizing?.firstSizingResults!!.isBlank()) {
                        return false
                    }
                }
                FruitFlowStep.HOT_WATER_TREATMENT_ENTRY -> {
                    if (
                            (reqParam.hydroCoolingEntry?.waterTemperatureHydroCooling != null && reqParam.hydroCoolingEntry?.waterTemperatureHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.timeofEntryHydroCooling != null && reqParam.hydroCoolingEntry?.timeofEntryHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.phLevelHydroCooling != null && reqParam.hydroCoolingEntry?.phLevelHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.chlorineLevelHydroCooling != null && reqParam.hydroCoolingEntry?.chlorineLevelHydroCooling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.HOT_WATER_TREATMENT_EXIT -> {
                    if (
                            (reqParam.hotWaterTreatmentExit?.durationHotWaterTreatment != null && reqParam.hotWaterTreatmentExit?.durationHotWaterTreatment!!.isBlank()) ||
                            (reqParam.hotWaterTreatmentExit?.timeofExitHotWaterTreatment != null && reqParam.hotWaterTreatmentExit?.timeofExitHotWaterTreatment!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.HYDRO_COOLING_ENTRY -> {
                    if (
                            (reqParam.hydroCoolingEntry?.chlorineLevelHydroCooling != null && reqParam.hydroCoolingEntry?.chlorineLevelHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.phLevelHydroCooling != null && reqParam.hydroCoolingEntry?.phLevelHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.timeofEntryHydroCooling != null && reqParam.hydroCoolingEntry?.timeofEntryHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingEntry?.waterTemperatureHydroCooling != null && reqParam.hydroCoolingEntry?.waterTemperatureHydroCooling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.HYDRO_COOLING_EXIT -> {
                    if (
                            (reqParam.hydroCoolingExit?.durationHydroCooling != null && reqParam.hydroCoolingExit?.durationHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingExit?.internalFruitTempHydroCooling != null && reqParam.hydroCoolingExit?.internalFruitTempHydroCooling!!.isBlank()) ||
                            (reqParam.hydroCoolingExit?.timeofExitHydroCooling != null && reqParam.hydroCoolingExit?.timeofExitHydroCooling!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.TRANSFER_TO_COLD_STORAGE -> {
                    if (
                            (reqParam.transferToColdStorage?.coldStorageTimeIn != null && reqParam.transferToColdStorage?.coldStorageTimeIn!!.isBlank()) ||
                            (reqParam.transferToColdStorage?.temperatureTransfer != null && reqParam.transferToColdStorage?.coldStorageTimeIn!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.REMOVAL_FROM_COLD_STORAGE -> {
                    if (
                            (reqParam.removalFromColdStorage?.coldStorageTimeOut != null && reqParam.removalFromColdStorage?.coldStorageTimeOut!!.isBlank()) ||
                            (reqParam.removalFromColdStorage?.temperatureRemoval != null && reqParam.removalFromColdStorage?.temperatureRemoval!!.isBlank())
                    ) {
                        return false
                    }
                }
                FruitFlowStep.TRANSPORT_DETAILS -> {
                    if (
                            (reqParam.transportDetails?.transportCost != null && reqParam.transportDetails?.transportCost!!.isBlank()) ||
                            (reqParam.transportDetails?.destinationTransport != null && reqParam.transportDetails?.destinationTransport!!.isBlank()) ||
                            (reqParam.transportDetails?.transportCurrency != null && reqParam.transportDetails?.transportCurrency!!.isBlank()) ||
                            (reqParam.transportDetails?.transportTemperature != null && reqParam.transportDetails?.transportTemperature!!.isBlank()) ||
                            (reqParam.transportDetails?.transportConditions != null && reqParam.transportDetails?.transportConditions!!.isBlank())
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            // Responder flow logic goes here.

        }
    }
}