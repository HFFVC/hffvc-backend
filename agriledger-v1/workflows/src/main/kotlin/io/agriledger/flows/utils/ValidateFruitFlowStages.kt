package io.agriledger.flows.utils

import io.agriledger.model.api.*
import io.agriledger.model.enumerations.FruitFlowStep
import io.agriledger.states.ServiceRequestState

class ValidateFruitFlowStages {
    fun validate(state: ServiceRequestState, req: FruitFlowDTO): Boolean {
        when (req.step) {

            FruitFlowStep.LARVAE_TESTING -> {
                if (req.larvaeTesting == null) return false
                if (req.fruitFlowStages.larvaeTesting.mandatory.toBoolean() && req.larvaeTesting?.larvaeTesting == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.TEMPERATURE_MEASUREMENT -> {
                if (req.temperatureMeasurement == null) return false
                if (req.fruitFlowStages.temperatureMeasurement.ambientTemp.toBoolean() && req.temperatureMeasurement?.ambientTemp == null) {
                    return false
                }
                if (req.fruitFlowStages.temperatureMeasurement.internalFruitTemperature.toBoolean() && req.temperatureMeasurement?.internalFruitTemp == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.QUALITY_INSPECTION -> {
                if (req.qualityInspection == null) return false

                if (req.fruitFlowStages.qualityInspection.fruitsAccepted.toBoolean() && req.qualityInspection?.fruitsAccepted == null) {
                    return false
                }
                if (req.fruitFlowStages.qualityInspection.fruitsRejected.toBoolean() && req.qualityInspection?.fruitsRejected == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.CARTON_FILLING_AND_PALLETIZING -> {
                if (req.cartonFillingAndPalletizing == null) return false

                req.cartonFillingAndPalletizing!!.forEach {
                    if (req.fruitFlowStages.cartonFillingAndPalletizing.startQRCodeFruits.toBoolean() && it.startQRCodeFruits == null) {
                        return false
                    }
                    if (req.fruitFlowStages.cartonFillingAndPalletizing.endQRCodeFruits.toBoolean() && it.endQRCodeFruits == null) {
                        return false
                    }
                    if (req.fruitFlowStages.cartonFillingAndPalletizing.startQRCodeBox.toBoolean() && it.startQRCodeBoxes == null) {
                        return false
                    }
                    if (req.fruitFlowStages.cartonFillingAndPalletizing.endQRCodeBox.toBoolean() && it.endQRCodeBoxes == null) {
                        return false
                    }
                }
                return true
            }

            FruitFlowStep.CLEANING_AND_TRIMMING -> {
                if (req.cleaningAndTrimming == null) return false
                if (req.fruitFlowStages.cleaningAndTrimming.mandatory.toBoolean() && req.cleaningAndTrimming?.dateTimeCleaningTrimming == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.COLD_ROOM_STORAGE_IN -> {
                if (req.coldRoomStorageIn == null) return false
                if (req.fruitFlowStages.coldRoomStorageIn.airflowTemperature.toBoolean() && req.coldRoomStorageIn?.airflowTemperatureRHColdStorage == null) {
                    return false
                }
                if (req.fruitFlowStages.coldRoomStorageIn.fruitTemperature.toBoolean() && req.coldRoomStorageIn?.fruitTemperatureColdStorage == null) {
                    return false
                }
                if (req.fruitFlowStages.coldRoomStorageIn.storageTimeIn.toBoolean() && req.coldRoomStorageIn?.storageTimeIn == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.COLD_ROOM_STORAGE_OUT -> {
                if (req.coldRoomStorageOut == null) return false

                if (req.fruitFlowStages.coldRoomStorageOut.fruitTemperature.toBoolean() && req.coldRoomStorageOut?.fruitTemperatureColdStorageOut == null) {
                    return false
                }
                if (req.fruitFlowStages.coldRoomStorageOut.storageTimeOut.toBoolean() && req.coldRoomStorageOut?.storageTimeOut == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.COLD_TUNNEL_LOADING -> {
                if (req.coldTunnelLoading == null) return false

                if (req.fruitFlowStages.coldTunnelLoading.destinationDateAndTime.toBoolean() && req.coldTunnelLoading?.destinationDateTimeColdTunnelLoading == null) {
                    return false
                }
                if (req.fruitFlowStages.coldTunnelLoading.reeferWallTemperature.toBoolean() && req.coldTunnelLoading?.reeferWallTemperatureColdTunnelLoading == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.FEED_PACKING_LINE_CONVEYOR_BELT -> {
                if (req.feedLineConveyorBelt == null) return false
                if (req.fruitFlowStages.feedPackingLineConveyorBelt.mandatory.toBoolean() && req.feedLineConveyorBelt?.packingLineTimeofEntry == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.FIRST_SIZING -> {
                if (req.firstSizing == null) return false
                if (req.fruitFlowStages.sizing.mandatory.toBoolean() && req.firstSizing?.firstSizingResults == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.FORCED_AIR_COOLING_ENTRY -> {
                if (req.forcedAirCoolingEntry == null) return false
                if (req.fruitFlowStages.forcedAirCoolingEntry.airflowTemperature.toBoolean() && req.forcedAirCoolingEntry?.airflowTemperatureRH == null) {
                    return false
                }
                if (req.fruitFlowStages.forcedAirCoolingEntry.fruitTemperatureAtEntry.toBoolean() && req.forcedAirCoolingEntry?.fruitTemperatureEntryForcedCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.forcedAirCoolingEntry.timeOfEntry.toBoolean() && req.forcedAirCoolingEntry?.entryTimeForcedCooling == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.FORCED_AIR_COOLING_REMOVAL -> {
                if (req.forcedAirCoolingRemoval == null) return false
                if (req.fruitFlowStages.removalFromForcedAirCooling.fruitTemperatureOnExit.toBoolean() && req.forcedAirCoolingRemoval?.fruitTemperatureRemovalForcedCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.removalFromForcedAirCooling.timeOfExit.toBoolean() && req.forcedAirCoolingRemoval?.removalTimeForcedCooling == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.FRUIT_WASHING -> {
                if (req.fruitWashing == null) return false
                if (req.fruitFlowStages.fruitWashing.PHLevel.toBoolean() && req.fruitWashing?.phLevel == null) {
                    return false
                }
                if (req.fruitFlowStages.fruitWashing.chlorineLevel.toBoolean() && req.fruitWashing?.chlorineLevel == null) {
                    return false
                }
                if (req.fruitFlowStages.fruitWashing.waterTemperature.toBoolean() && req.fruitWashing?.waterTemperature == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.GRADING -> {
                if (req.grading == null) return false
                if (req.fruitFlowStages.grading.gradingResults.toBoolean() && req.grading?.gradingResults == null) {
                    return false
                }
                if (req.fruitFlowStages.grading.fruitsRemoved.toBoolean() && req.grading?.weightofRemovedFruit == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.HOT_WATER_TREATMENT_ENTRY -> {
                if (req.hotWaterTreatmentEntry == null) return false
                if (req.fruitFlowStages.hotWaterTreatmentEntry.PHLevel.toBoolean() && req.hotWaterTreatmentEntry?.phLevelHotWaterTreatment == null) {
                    return false
                }
                if (req.fruitFlowStages.hotWaterTreatmentEntry.chlorineLevel.toBoolean() && req.hotWaterTreatmentEntry?.chlorineLevelHotWaterTreatment == null) {
                    return false
                }
                if (req.fruitFlowStages.hotWaterTreatmentEntry.waterTemperature.toBoolean() && req.hotWaterTreatmentEntry?.waterTemperatureHotWaterTreatment == null) {
                    return false
                }
                if (req.fruitFlowStages.hotWaterTreatmentEntry.timeOfEntry.toBoolean() && req.hotWaterTreatmentEntry?.timeofEntryHotWaterTreatment == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.HOT_WATER_TREATMENT_EXIT -> {
                if (req.hotWaterTreatmentExit == null) return false
                if (req.fruitFlowStages.hotWaterTreatmentExit.durationOfTreatment.toBoolean() && req.hotWaterTreatmentExit?.durationHotWaterTreatment == null) {
                    return false
                }
                if (req.fruitFlowStages.hotWaterTreatmentExit.timeOfExit.toBoolean() && req.hotWaterTreatmentExit?.timeofExitHotWaterTreatment == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.HYDRO_COOLING_ENTRY -> {
                if (req.hydroCoolingEntry == null) return false
                if (req.fruitFlowStages.hydroCoolingEntry.PHLevel.toBoolean() && req.hydroCoolingEntry?.phLevelHydroCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.hydroCoolingEntry.chlorineLevel.toBoolean() && req.hydroCoolingEntry?.chlorineLevelHydroCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.hydroCoolingEntry.waterTemperature.toBoolean() && req.hydroCoolingEntry?.waterTemperatureHydroCooling == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.HYDRO_COOLING_EXIT -> {
                if (req.hydroCoolingExit == null) return false
                if (req.fruitFlowStages.hydroCoolingExit.duration.toBoolean() && req.hydroCoolingExit?.durationHydroCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.hydroCoolingExit.internalFruitTemperature.toBoolean() && req.hydroCoolingExit?.internalFruitTempHydroCooling == null) {
                    return false
                }
                if (req.fruitFlowStages.hydroCoolingExit.timeOfExit.toBoolean() && req.hydroCoolingExit?.timeofExitHydroCooling == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.PRE_COOLING_REEFERS -> {
                if (req.preCoolingReefers == null) return false
                if (req.fruitFlowStages.preCoolingReefers.mandatory.toBoolean() && req.preCoolingReefers?.reeferWallTemperature == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.TRANSFER_TO_COLD_STORAGE -> {
                if (req.transferToColdStorage == null) return false
                if (req.fruitFlowStages.transferToColdStorage.storageTimeIn.toBoolean() && req.transferToColdStorage?.coldStorageTimeIn == null) {
                    return false
                }
                if (req.fruitFlowStages.transferToColdStorage.temperature.toBoolean() && req.transferToColdStorage?.temperatureTransfer == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.REMOVAL_FROM_COLD_STORAGE -> {
                if (req.removalFromColdStorage == null) return false
                if (req.fruitFlowStages.removalFromColdStorage.storageTimeOut.toBoolean() && req.removalFromColdStorage?.coldStorageTimeOut == null) {
                    return false
                }
                if (req.fruitFlowStages.removalFromColdStorage.temperature.toBoolean() && req.removalFromColdStorage?.temperatureRemoval == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.SAMPLING_DETAILS -> {
                if (req.samplingDetails == null) return false
                if (req.fruitFlowStages.sampling.samplesTaken.toBoolean() && req.samplingDetails?.samplesTaken == null) {
                    return false
                }
                if (req.fruitFlowStages.sampling.temperature.toBoolean() && req.samplingDetails?.samplingTemperature == null) {
                    return false
                }
                if (req.fruitFlowStages.sampling.timestamp.toBoolean() && req.samplingDetails?.dateAndTimeofSampling == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.SECOND_SIZING -> {
                if (req.secondSizing == null) return false
                if (req.fruitFlowStages.secondSizing.mandatory.toBoolean() && req.secondSizing?.secondSizingResults == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.SHIPPING_DETAILS -> {
                if (req.shippingDetails == null) return false
                if (req.fruitFlowStages.shippingDetails.mandatory.toBoolean() && req.shippingDetails?.shippingDetails == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.TEMPERATURE_READING_PACKED_LOT -> {
                if (req.temperatureReadingPackedLot == null) return false
                if (req.fruitFlowStages.temperatureReadingPackedLot.ambientTemperature.toBoolean() && req.temperatureReadingPackedLot?.ambienttemperaturePacked == null) {
                    return false
                }
                if (req.fruitFlowStages.temperatureReadingPackedLot.internalFruitTemperature.toBoolean() && req.temperatureReadingPackedLot?.internalFruitTemperaturePacked == null) {
                    return false
                }
                return true
            }

            FruitFlowStep.TRANSPORT_DETAILS -> {
                if (req.transportDetails == null) return false
                if (req.fruitFlowStages.transportDetails.currency.toBoolean() && req.transportDetails?.transportCurrency == null) {
                    return false
                }
                if (req.fruitFlowStages.transportDetails.departureDateAndTime.toBoolean() && req.transportDetails?.departureDateTimeTransport == null) {
                    return false
                }
                if (req.fruitFlowStages.transportDetails.destination.toBoolean() && req.transportDetails?.destinationTransport == null) {
                    return false
                }
                if (req.fruitFlowStages.transportDetails.temperature.toBoolean() && req.transportDetails?.transportTemperature == null) {
                    return false
                }
                if (req.fruitFlowStages.transportDetails.transportConditions.toBoolean() && req.transportDetails?.transportConditions == null) {
                    return false
                }
                if (req.fruitFlowStages.transportDetails.transportCost.toBoolean() && req.transportDetails?.transportCost == null) {
                    return false
                }

                return true
            }

            else -> {
                return false
            }
        }
    }

    fun validateStages(state: ServiceRequestState, req: FruitFlowStagesDTO): Boolean {
        if (
                (req.cartonFillingAndPalletizing.mandatory.toBoolean() && state.fruitFlowData?.cartonFillingAndPalletizingData == null) ||
                (req.cleaningAndTrimming.mandatory.toBoolean() && state.fruitFlowData?.cleaningAndTrimmingData == null) ||
                (req.coldRoomStorageIn.mandatory.toBoolean() && state.fruitFlowData?.coldRoomStorageInData == null) ||
                (req.coldRoomStorageOut.mandatory.toBoolean() && state.fruitFlowData?.coldRoomStorageOutData == null) ||
                (req.coldTunnelLoading.mandatory.toBoolean() && state.fruitFlowData?.coldTunnelLoadingData == null) ||
                (req.feedPackingLineConveyorBelt.mandatory.toBoolean() && state.fruitFlowData?.feedPackingLineConveyorData == null) ||
                (req.forcedAirCoolingEntry.mandatory.toBoolean() && state.fruitFlowData?.forcedAirCoolingEntryData == null) ||
                (req.fruitWashing.mandatory.toBoolean() && state.fruitFlowData?.fruitWashingData == null) ||
                (req.grading.mandatory.toBoolean() && state.fruitFlowData?.gradingData == null) ||
                (req.hotWaterTreatmentEntry.mandatory.toBoolean() && state.fruitFlowData?.hotWaterTreatmentEntryData == null) ||
                (req.hotWaterTreatmentExit.mandatory.toBoolean() && state.fruitFlowData?.hotWaterTreatmentExitData == null) ||
                (req.hydroCoolingEntry.mandatory.toBoolean() && state.fruitFlowData?.hydroCoolingEntryData == null) ||
                (req.hydroCoolingExit.mandatory.toBoolean() && state.fruitFlowData?.hydroCoolingExitData == null) ||
                (req.larvaeTesting.mandatory.toBoolean() && state.fruitFlowData?.larvaeTestingData == null) ||
                (req.preCoolingReefers.mandatory.toBoolean() && state.fruitFlowData?.preCoolingReefersData == null) ||
                (req.qualityInspection.mandatory.toBoolean() && state.fruitFlowData?.qualityInspectionData == null) ||
                (req.removalFromColdStorage.mandatory.toBoolean() && state.fruitFlowData?.removalFromColdStorageData == null) ||
                (req.removalFromForcedAirCooling.mandatory.toBoolean() && state.fruitFlowData?.forcedAirCoolingRemovalData == null) ||
                (req.sampling.mandatory.toBoolean() && state.fruitFlowData?.samplingDetailsData == null) ||
                (req.secondSizing.mandatory.toBoolean() && state.fruitFlowData?.secondSizingData == null) ||
                (req.shippingDetails.mandatory.toBoolean() && state.fruitFlowData?.shippingDetailsData == null) ||
                (req.sizing.mandatory.toBoolean() && state.fruitFlowData?.firstSizingData == null) ||
                (req.temperatureMeasurement.mandatory.toBoolean() && state.fruitFlowData?.temperatureMeasurementData == null) ||
                (req.temperatureReadingPackedLot.mandatory.toBoolean() && state.fruitFlowData?.temperatureReadingPackedLotData == null) ||
                (req.transferToColdStorage.mandatory.toBoolean() && state.fruitFlowData?.transferTocoldStorageData == null)
        ) {
            throw Exception("One or more mandatory stages have not been completed.")
        }

        return true
    }
}