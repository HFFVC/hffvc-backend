package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ServiceRequestCreationDTO(val id: String,
                                val farmerId: String,
                                val producer: String,
                                val location: String,
                                val department: String,
                                val town: String,
                                val requestedOn: String,
                                val estimatedNoFruits: String,
                                val product: String,
                                val status: String,
                                val displayId: String,
                                val phoneNo: String?,
                                val cin: String?,
                                val nif: String?
)