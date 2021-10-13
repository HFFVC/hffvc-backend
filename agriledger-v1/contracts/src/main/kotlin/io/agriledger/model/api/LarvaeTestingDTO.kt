package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class LarvaeTestingDTO(val larvaeTesting: String?
                       , val rejectReason: String? = null
)