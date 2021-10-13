package io.agriledger.model.api

import io.agriledger.model.*
import io.agriledger.model.enumerations.Product
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EditServiceRequestDTO(val id: String,
                            val farmerId: String,
                            val producer: String,
                            val location: String,
                            val department: String,
                            val town: String,
                            val requestedOn: String,
                            val estimatedNoFruits: String,
                            val product: String,
                            val status: String,
                            val displayId: String? = null,
                            val phoneNo: String? = null,
                            val cin: String? = null,
                            val nif: String? = null
)