package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UpdateIdentificationNoDTO(val id: String,
                                val cin: String?,
                                val nif: String?
)