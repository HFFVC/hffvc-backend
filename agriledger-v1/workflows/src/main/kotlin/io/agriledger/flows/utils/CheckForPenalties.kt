package io.agriledger.flows.utils

import io.agriledger.model.PenaltyDataModel
import io.agriledger.model.enumerations.*
import io.agriledger.states.ServiceRequestState
import java.time.Duration
import java.time.LocalDateTime

class CheckForPenalties {
    fun check(vaultData: ServiceRequestState): ServiceRequestState {

        var existingPenalties = vaultData.penaltiesIncurred.toMutableList()

        checkDelayPenalty(vaultData, existingPenalties)
        arrivalAtPackhousePenalty(vaultData, existingPenalties)
        receivingAtPackhousePenalty(vaultData, existingPenalties)
        temperatureBreachPenalty(vaultData, existingPenalties)
        hotWaterTreatmentPenalty(vaultData, existingPenalties)
        hydroCoolingPenalty(vaultData, existingPenalties)
        coldRoomStorageDelayPenalty(vaultData, existingPenalties)
        coldRoomStorageBreachPenalty(vaultData, existingPenalties)
        lossOfBoxPenalty(vaultData, existingPenalties)

        return vaultData.copy(
                penaltiesIncurred = existingPenalties
        )
    }

    private fun checkDelayPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.acceptData != null) {
            val penaltyCalculator = PenaltyCalculator()
            // checking for delay penalty
            val requestCreatedOn = vaultData.creationData.requestedOn
            val scheduledOn = if (vaultData.lotRejectData != null) {
                vaultData.lotRejectData?.rejectedOn
            } else {
                vaultData.acceptData?.pickupAddedOn
            }

            if (scheduledOn != null) {
                val delayOf = Duration.between(requestCreatedOn, scheduledOn).toDays()
                if (delayOf >= 11) {
                    val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                        (vaultData.creationData.estimatedNoFruits / 10)
                    } else {
                        (vaultData.creationData.estimatedNoFruits / 10) + 1
                    }

                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.DELAYED_BY_11,
                            vaultData.creationData.product,
                            noOfBoxes = noOfBoxes
                    )
                    existingPenalties.add(
                            PenaltyDataModel(
                                    type = PenaltyType.DELAYED_BY_11,
                                    amount = penaltyAmount,
                                    calculatedOn = LocalDateTime.now(),
                                    leviedOn = Roles.LSP
                            )
                    )
                } else if (delayOf in 8..10) {
                    val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                        (vaultData.creationData.estimatedNoFruits / 10)
                    } else {
                        (vaultData.creationData.estimatedNoFruits / 10) + 1
                    }
                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.DELAYED_BY_7,
                            vaultData.creationData.product,
                            noOfBoxes = noOfBoxes
                    )

                    existingPenalties.add(
                            PenaltyDataModel(
                                    type = PenaltyType.DELAYED_BY_7,
                                    amount = penaltyAmount,
                                    calculatedOn = LocalDateTime.now(),
                                    leviedOn = Roles.LSP
                            ))
                }
            }
        }
    }

    private fun arrivalAtPackhousePenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.withProducerData != null && vaultData.enrouteToPackhouseData != null) {
            val penaltyCalculator = PenaltyCalculator()

            val collectionPointTime = vaultData.withProducerData?.withProducerTimeStamp
            val arrivedAtPackhouseTime = vaultData.arrivedAtPackhouseData?.arrivedAtPackhouseAddedOn

            val delayOf = Duration.between(collectionPointTime, arrivedAtPackhouseTime).toHours()

            val acceptableDelay = when (vaultData.creationData.product) {
                Product.MANGO -> 36
                Product.AVOCADO -> 16
                Product.PINEAPPLE -> 16
            }
            if (delayOf > acceptableDelay) {
                val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                    (vaultData.creationData.estimatedNoFruits / 10)
                } else {
                    (vaultData.creationData.estimatedNoFruits / 10) + 1
                }
                val penaltyAmount = penaltyCalculator.calculatePenalty(
                        PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                        vaultData.creationData.product,
                        soldBoxes = noOfBoxes
                )
                breachAndDelayPenalties(existingPenalties,
                        DelayAndBreachPenalty.ARRIVAL_AT_PACKHOUSE,
                        penaltyAmount,
                        Roles.LSP
                )
            }

        }

    }

    private fun receivingAtPackhousePenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (
                vaultData.withProducerData != null &&
                vaultData.enrouteToPackhouseData != null &&
                vaultData.enrouteToProducerData != null &&
                vaultData.arrivedAtPackhouseData != null
        ) {
            val arrivedAtPackhouseOn = vaultData.enrouteToPackhouseData?.enroutePackhouseAddedOn
            val timeOfAdmittance = vaultData.arrivedAtPackhouseData?.timeOfAdmittance

            val delayOf = Duration.between(arrivedAtPackhouseOn, timeOfAdmittance).toHours()

            val acceptableDelay = when (vaultData.creationData.product) {
                Product.MANGO -> 36
                Product.AVOCADO -> 3
                Product.PINEAPPLE -> 4
            }

            if (delayOf > acceptableDelay) {
                val penaltyCalculator = PenaltyCalculator()
                if (vaultData.saleIds.size == 0) {
                    val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                        (vaultData.creationData.estimatedNoFruits / 10)
                    } else {
                        (vaultData.creationData.estimatedNoFruits / 10) + 1
                    }
                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                            vaultData.creationData.product,
                            unsoldBoxes = noOfBoxes
                    )
                    breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.RECEIVED_AT_PACKHOUSE, penaltyAmount, Roles.LSP)
                } else {
                    var soldBoxes = 0
                    vaultData.saleIds.forEach {
                        soldBoxes += it.boxesSold
                    }

                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                            vaultData.creationData.product,
                            soldBoxes = soldBoxes,
                            unsoldBoxes = vaultData.remainingBoxes!!
                    )
                    breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.RECEIVED_AT_PACKHOUSE, penaltyAmount, Roles.LSP)
                }
            }
        }
    }

    private fun temperatureBreachPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        // checking for temperature breach at temperature measurement stage of fruit flow
        if (vaultData.arrivedAtPackhouseData != null && vaultData.fruitFlowData?.larvaeTestingData != null && vaultData.fruitFlowData?.temperatureMeasurementData != null) {
            val acceptableTemperature = when (vaultData.creationData.product) {
                Product.MANGO -> 35
                Product.AVOCADO -> 30
                Product.PINEAPPLE -> 33
            }

            if (vaultData.fruitFlowData?.temperatureMeasurementData!!.internalFruitTemp!! > acceptableTemperature) {

                val penaltyCalculator = PenaltyCalculator()
                if (vaultData.saleIds.size == 0) {
                    val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                        (vaultData.creationData.estimatedNoFruits / 10)
                    } else {
                        (vaultData.creationData.estimatedNoFruits / 10) + 1
                    }
                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                            vaultData.creationData.product,
                            unsoldBoxes = noOfBoxes
                    )

                    breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.TEMPERATURE_MEASUREMENT, penaltyAmount, Roles.LSP)
                } else {
                    // unsold boxes * 10 (assuming that each box contains 10 fruits)
                    var soldBoxes = 0
                    vaultData.saleIds.forEach {
                        soldBoxes += it.boxesSold
                    }

                    val penaltyAmount = penaltyCalculator.calculatePenalty(
                            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                            vaultData.creationData.product,
                            soldBoxes = soldBoxes,
                            unsoldBoxes = vaultData.remainingBoxes!!
                    )

                    breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.TEMPERATURE_MEASUREMENT, penaltyAmount, Roles.LSP)
                }
            }
        }
    }

    private fun hotWaterTreatmentPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.fruitFlowData != null && vaultData.fruitFlowData?.hotWaterTreatmentEntryData != null && vaultData.fruitFlowData?.hotWaterTreatmentExitData != null) {
            // only applicable for MANGO
            val waterTemperature = vaultData.fruitFlowData?.hotWaterTreatmentEntryData?.waterTemperatureHotWaterTreatment

            val timeOfEntry = vaultData.fruitFlowData?.hotWaterTreatmentEntryData?.timeofEntryHotWaterTreatment
            val timeOfExit = vaultData.fruitFlowData?.hotWaterTreatmentExitData?.timeofExitHotWaterTreatment

            val duration = Duration.between(timeOfEntry, timeOfExit).toMinutes()

            if (waterTemperature != null) {
                if (duration > 10 && waterTemperature > 47) {
                    val penaltyCalculator = PenaltyCalculator()
                    if (vaultData.saleIds.size == 0) {
                        val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                            (vaultData.creationData.estimatedNoFruits / 10)
                        } else {
                            (vaultData.creationData.estimatedNoFruits / 10) + 1
                        }
                        val penaltyAmount = penaltyCalculator.calculatePenalty(
                                PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                vaultData.creationData.product,
                                unsoldBoxes = noOfBoxes
                        )
                        breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.HOT_WATER_TREATMENT, penaltyAmount, Roles.LSP)
                    } else {
                        // unsold boxes * 10 (assuming that each box contains 10 fruits)
                        var soldBoxes = 0
                        vaultData.saleIds.forEach {
                            soldBoxes += it.boxesSold
                        }

                        val penaltyAmount = penaltyCalculator.calculatePenalty(
                                PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                vaultData.creationData.product,
                                soldBoxes = soldBoxes,
                                unsoldBoxes = vaultData.remainingBoxes!!
                        )

                        breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.HOT_WATER_TREATMENT, penaltyAmount, Roles.LSP)
                    }
                }
            }
        }
    }

    private fun hydroCoolingPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.fruitFlowData != null) {
            //
            if (vaultData.fruitFlowData?.hydroCoolingEntryData != null && vaultData.fruitFlowData?.hydroCoolingExitData != null) {
                if (vaultData.fruitFlowData?.hydroCoolingExitData?.internalFruitTempHydroCooling!! > 31) {
                    // only applicable to MANGO
                    val penaltyCalculator = PenaltyCalculator()
                    if (vaultData.saleIds.size == 0) {
                        val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                            (vaultData.creationData.estimatedNoFruits / 10)
                        } else {
                            (vaultData.creationData.estimatedNoFruits / 10) + 1
                        }
                        val penaltyAmount = penaltyCalculator.calculatePenalty(
                                PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                vaultData.creationData.product,
                                unsoldBoxes = noOfBoxes
                        )

                        breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.HYDRO_COOLING, penaltyAmount, Roles.LSP)
                    } else {
                        // unsold boxes * 10 (assuming that each box contains 10 fruits)
                        var soldBoxes = 0
                        vaultData.saleIds.forEach {
                            soldBoxes += it.boxesSold
                        }

                        val penaltyAmount = penaltyCalculator.calculatePenalty(
                                PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                vaultData.creationData.product,
                                soldBoxes = soldBoxes,
                                unsoldBoxes = vaultData.remainingBoxes!!
                        )

                        breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.HYDRO_COOLING, penaltyAmount, Roles.LSP)
                    }
                }
            }
        }
    }

    private fun coldRoomStorageDelayPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.fruitFlowData != null) {
            if (vaultData.fruitFlowData?.coldRoomStorageInData != null && vaultData.fruitFlowData?.coldRoomStorageOutData != null) {
                val acceptableDelay = 7

                val storageTimeIn = vaultData.fruitFlowData?.coldRoomStorageInData?.storageTimeIn
                val storageTimeOut = vaultData.fruitFlowData?.coldRoomStorageOutData?.storageTimeOut

                val differenceInDays = Duration.between(storageTimeIn, storageTimeOut).toDays()

                val penaltyCalculator = PenaltyCalculator()

                if (differenceInDays > acceptableDelay) {
                    val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                        (vaultData.creationData.estimatedNoFruits / 10)
                    } else {
                        (vaultData.creationData.estimatedNoFruits / 10) + 1
                    }
                    val daysDelayPenalty = penaltyCalculator.calculatePenalty(
                            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                            vaultData.creationData.product,
                            unsoldBoxes = noOfBoxes
                    )

                    breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.COLD_ROOM_STORAGE_TIME_DELAY, daysDelayPenalty, Roles.LSP)
                }
            }
        }
    }

    private fun coldRoomStorageBreachPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.fruitFlowData != null) {
            if (vaultData.fruitFlowData?.coldRoomStorageInData != null && vaultData.fruitFlowData?.coldRoomStorageOutData != null) {

                val acceptableTemperatures = when (vaultData.creationData.product) {
                    Product.MANGO -> 10
                    Product.AVOCADO -> 4
                    Product.PINEAPPLE -> 7
                }

                val acceptableStorageTime = when (vaultData.creationData.product) {
                    Product.MANGO -> 24
                    Product.AVOCADO -> 12
                    Product.PINEAPPLE -> 3
                }

                val storageTimeIn = vaultData.fruitFlowData?.coldRoomStorageInData?.storageTimeIn
                val storageTimeOut = vaultData.fruitFlowData?.coldRoomStorageOutData?.storageTimeOut
                val coldStorageInTemperature = vaultData.fruitFlowData?.coldRoomStorageInData?.airflowTemperatureRHColdStorage
                val differenceInHours = Duration.between(storageTimeIn, storageTimeOut).toHours()
                val penaltyCalculator = PenaltyCalculator()
                var temperaturePenalty = 0f

                if (coldStorageInTemperature != null) {
                    if (coldStorageInTemperature > acceptableTemperatures && differenceInHours > acceptableStorageTime) {

                        if (vaultData.saleIds.size == 0) {
                            val noOfBoxes = if (vaultData.creationData.estimatedNoFruits % 10 == 0) {
                                (vaultData.creationData.estimatedNoFruits / 10)
                            } else {
                                (vaultData.creationData.estimatedNoFruits / 10) + 1
                            }
                            temperaturePenalty = penaltyCalculator.calculatePenalty(
                                    PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                    vaultData.creationData.product,
                                    unsoldBoxes = noOfBoxes
                            )
                        } else {
                            // unsold boxes * 10 (assuming that each box contains 10 fruits)
                            var soldBoxes = 0
                            vaultData.saleIds.forEach {
                                soldBoxes += it.boxesSold
                            }

                            temperaturePenalty = penaltyCalculator.calculatePenalty(
                                    PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                                    vaultData.creationData.product,
                                    soldBoxes = soldBoxes,
                                    unsoldBoxes = vaultData.remainingBoxes!!
                            )
                        }
//                        existingPenalties.add(
//                                PenaltyDataModel(
//                                        type = PenaltyType.COLD_ROOM_STORAGE_TEMPERATURE_BREACH,
//                                        amount = temperaturePenalty,
//                                        calculatedOn = LocalDateTime.now(),
//                                        leviedOn = Roles.LSP
//                                )
//                        )
                        if (temperaturePenalty > 0f) {
                            breachAndDelayPenalties(existingPenalties, DelayAndBreachPenalty.COLD_ROOM_STORAGE_TEMPERATURE_BREACH, temperaturePenalty, Roles.LSP)
                        }
                    }
                }
            }
        }
    }

    private fun lossOfBoxPenalty(vaultData: ServiceRequestState, existingPenalties: MutableList<PenaltyDataModel>) {
        if (vaultData.fruitFlowData != null) {
            if (vaultData.fruitFlowData?.qualityInspectionData != null && vaultData.fruitFlowData?.gradingData != null && vaultData.fruitFlowData?.cartonFillingAndPalletizingData != null) {

                var boxesAtCartonFillingAndPalletizing = 0
                vaultData.fruitFlowData?.cartonFillingAndPalletizingData?.forEach {
                    lot -> if (lot.totalBoxes != null) {
                    boxesAtCartonFillingAndPalletizing += lot.totalBoxes!!
                }
                }

                val expectedFruitsAvailable = vaultData.withProducerData?.fruitsHarvested!! - (
                        vaultData.withProducerData?.fruitRejected!! +
                        vaultData.fruitFlowData?.qualityInspectionData?.fruitsRejected!! +
                                vaultData.fruitFlowData?.gradingData?.weightofRemovedFruit!!.toInt()
                        )

                val expectedBoxes = if (expectedFruitsAvailable % 10 == 0) {
                    (expectedFruitsAvailable / 10)
                } else {
                    (expectedFruitsAvailable / 10) + 1
                }

//                if (boxesAtCartonFillingAndPalletizing != null) {
                    if (boxesAtCartonFillingAndPalletizing < expectedBoxes) {
                        val lostBoxes = expectedBoxes - boxesAtCartonFillingAndPalletizing
                        val penaltyCalculator = PenaltyCalculator()
                        val penaltyAmount = penaltyCalculator.calculatePenalty(
                                PenaltyType.LOSS_OF_BOXES,
                                vaultData.creationData.product,
                                noOfBoxes = lostBoxes
                        )

                        existingPenalties.add(
                                PenaltyDataModel(
                                        type = PenaltyType.LOSS_OF_BOXES,
                                        calculatedOn = LocalDateTime.now(),
                                        amount = penaltyAmount,
                                        leviedOn = Roles.LSP
                                )
                        )
                    }
//                }
            }
        }
    }

    private fun breachExists(existingPenalties: MutableList<PenaltyDataModel>): Boolean {
        existingPenalties.forEach {
            if (it.delayAndBreaches.size > 0) {
                return true
            }
        }
        return false
    }

    private fun breachAndDelayPenalties(
            existingPenalties: MutableList<PenaltyDataModel>,
            penaltyType: DelayAndBreachPenalty,
            amount: Float,
            leviedOn: Roles
    ) {
        if (!breachExists(existingPenalties)) {
            val newPenalty = mutableListOf<DelayAndBreachPenalty>()
            newPenalty.add(penaltyType)
            existingPenalties.add(PenaltyDataModel(
                    type = PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH,
                    calculatedOn = LocalDateTime.now(),
                    amount = amount,
                    currency = Currency.USD,
                    leviedOn = leviedOn,
                    delayAndBreaches = newPenalty
            ))
        } else {
            // getting existing temperature breach penalty
            existingPenalties.forEach {
                if (it.type == PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH) {
                    it.delayAndBreaches.add(penaltyType)
                }
            }
        }
    }
}