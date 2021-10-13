package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class UnloadingAtBuyerModel(val dateAndTimeOfUnLoading: LocalDateTime,
                            val totalNoofBoxesUnLoaded: Int
)