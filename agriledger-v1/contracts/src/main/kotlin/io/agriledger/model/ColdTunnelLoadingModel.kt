package io.agriledger.model

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class ColdTunnelLoadingModel(val destinationDateTimeColdTunnelLoading: LocalDateTime? = null,
                             val reeferWallTemperatureColdTunnelLoading: Float? = null
)