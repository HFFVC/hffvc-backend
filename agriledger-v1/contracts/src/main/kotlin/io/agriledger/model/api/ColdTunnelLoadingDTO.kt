package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ColdTunnelLoadingDTO(val destinationDateTimeColdTunnelLoading: String?,
                           val reeferWallTemperatureColdTunnelLoading: String?
)