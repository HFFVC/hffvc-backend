package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ArrivalAndDestinationModel(val arrivalTimestamp: LocalDateTime,
                                 val destination: String
)