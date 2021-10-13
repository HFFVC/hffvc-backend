package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class AcceptServiceRequestDTO(val id: String,
                              val status: String,
                              val pickupAddedOn: String,
                              val collectorName: String,
                              val collectionPoint: String,
                              val dropOffAtPackhouse: String,
                              val dateOfPickup: String,
                              val scheduled: String,
                              val amount: String,
                              val scheduledAfter: String
)