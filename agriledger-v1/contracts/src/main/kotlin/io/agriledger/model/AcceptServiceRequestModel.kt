package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class AcceptServiceRequestModel(val pickupAddedOn: LocalDateTime,
                                     val collectorName: String,
                                     val collectionPoint: String,
                                     val dropOffAtPackhouse: String,
                                     val dateOfPickup: LocalDateTime,
                                     val scheduled: String,
                                     val amount: Int,
                                     val scheduledAfter: Int
)