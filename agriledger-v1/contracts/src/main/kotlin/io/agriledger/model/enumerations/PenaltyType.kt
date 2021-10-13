package io.agriledger.model.enumerations
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class PenaltyType {
    DELAYED_BY_7,
    DELAYED_BY_11,
//    ARRIVAL_AT_PACKHOUSE,
//    RECEIVED_AT_PACKHOUSE,
    TIME_DELAY_AND_TEMPERATURE_BREACH,
//    HOT_WATER_TREATMENT,
//    HYDRO_COOLING,
//    COLD_ROOM_STORAGE_TIME_DELAY,
//    COLD_ROOM_STORAGE_TEMPERATURE_BREACH,
    LOSS_OF_BOXES
}