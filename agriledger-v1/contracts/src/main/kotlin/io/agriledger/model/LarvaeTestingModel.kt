package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class LarvaeTestingModel(val larvaeTesting: String? = null
                         , val rejectReason: String? = null
)