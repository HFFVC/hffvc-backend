package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ServiceRequestDataModel(val id: String,
                              val participants: MutableList<String> = mutableListOf(),
                              val signatories: MutableSet<String> = mutableSetOf(),
                              val txHash: String,
                              val displayId: String?,
                              val status: String,
                              val producerId: String,
                              val location: String,
                              val department: String,
                              val town: String,
                              val requestedOn: LocalDateTime,
                              val estimatedNoOfFruits: Int,
                              val product: String,
                              val collector: String?,
                              val collectionPoint: String?,
                              val batchId: String? = null,
                              val saleIds: MutableList<String> = mutableListOf(),
                              val remainingBoxes: Int? = null
)