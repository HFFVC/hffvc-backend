package io.agriledger.model

import io.agriledger.model.enumerations.Currency
import io.agriledger.model.enumerations.Roles
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class SalesDistributionModel(val role: Roles,
                             val amount: Float,
                             val currency: Currency,
                             val advanceConsidered: Float? = null,
                             val organizationName: String? = null,
                             val organizationKey: String? = null
)