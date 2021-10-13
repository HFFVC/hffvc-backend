package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class CleaningAndTrimmingModel(val dateTimeCleaningTrimming: LocalDateTime? = null)