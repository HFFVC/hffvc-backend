package io.agriledger.model

import io.agriledger.model.enumerations.Currency
import io.agriledger.model.enumerations.DelayAndBreachPenalty
import io.agriledger.model.enumerations.PenaltyType
import io.agriledger.model.enumerations.Roles
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class PenaltyDataModel(val type: PenaltyType,
                       val calculatedOn: LocalDateTime,
                       val amount: Float,
                       val leviedOn: Roles,
                       val currency: Currency=Currency.USD,
                       val delayAndBreaches: MutableList<DelayAndBreachPenalty> = mutableListOf()
)