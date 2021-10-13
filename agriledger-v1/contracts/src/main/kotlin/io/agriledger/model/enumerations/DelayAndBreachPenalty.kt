package io.agriledger.model.enumerations
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class DelayAndBreachPenalty {
    ARRIVAL_AT_PACKHOUSE,
    RECEIVED_AT_PACKHOUSE,
    TEMPERATURE_MEASUREMENT,
    HOT_WATER_TREATMENT,
    HYDRO_COOLING,
    COLD_ROOM_STORAGE_TIME_DELAY,
    COLD_ROOM_STORAGE_TEMPERATURE_BREACH
}