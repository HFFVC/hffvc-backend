package io.agriledger.model

import io.agriledger.model.enumerations.Product
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class ServiceRequestCreationModel(val farmerId: String,
                                       val producer: String,
                                       val location: String,
                                       val department: String,
                                       val town: String,
                                       val requestedOn: LocalDateTime,
                                       val estimatedNoFruits: Int,
                                       val product: Product,
                                       val status: String,
                                       val displayId: String? = null,
                                       val phoneNo: String? = null,
                                       val cin: String? = null,
                                       val nif: String? = null
)