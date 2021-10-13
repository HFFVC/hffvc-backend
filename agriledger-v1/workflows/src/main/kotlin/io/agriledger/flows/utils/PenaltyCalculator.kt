package io.agriledger.flows.utils

import io.agriledger.model.enumerations.PenaltyType
import io.agriledger.model.enumerations.Product

class PenaltyCalculator {

    fun calculatePenalty(type: PenaltyType, product: Product, noOfBoxes: Int = 0, soldBoxes: Int = 0, unsoldBoxes: Int = 0): Float {

        /* Older logic for penalty computation. Zero to be returned based on latest requirements.
        // ============================================================================
        val avgWeights = mutableMapOf<Product, Float>()
        avgWeights[Product.MANGO] = 4.5f
        avgWeights[Product.AVOCADO] = 5.0f
        avgWeights[Product.PINEAPPLE] = 12.25f

        when (type) {
            PenaltyType.DELAYED_BY_7 -> {
                return (Constants.FRKF * noOfBoxes * avgWeights.getValue(product))
            }
            PenaltyType.DELAYED_BY_11 -> {
                return (2 * Constants.FRKF * noOfBoxes * avgWeights.getValue(product))
            }
            PenaltyType.TIME_DELAY_AND_TEMPERATURE_BREACH -> {
                return ((Constants.FRKF * soldBoxes * avgWeights.getValue(product)) + (2 * Constants.FRKF * unsoldBoxes * avgWeights.getValue(product)))
            }
            PenaltyType.LOSS_OF_BOXES -> {
                return (2 * Constants.FRKF * avgWeights.getValue(product) * noOfBoxes)
            }
        }

        ===================================================================
        */

        return 0f
    }
}