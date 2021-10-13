package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class EnrouteToProducerModel(val plannedPickUpAddedOn: LocalDateTime
                             , val enrouteAdditionalNote: String

)