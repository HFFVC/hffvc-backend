package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class FeedPackingLineConveyorBeltModel(val packingLineTimeofEntry: LocalDateTime? = null)