package io.agriledger.flows

import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.*
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import java.lang.Exception

object PenaltyDistribution {
    class Distribution(val serviceRequestId: String, val vaultService: CordaRPCOps) {
        fun calculateDistribution(): PenaltyDistributionModel {
            val wantedStateRef = QueryVault().queryServiceRequestById(serviceRequestId, Vault.StateStatus.UNCONSUMED, vaultService)

            if (wantedStateRef == null) {
                throw Exception("Sale ID ${serviceRequestId} not found.")
            }
            val vaultData = wantedStateRef.state.data
            val penalties = vaultData.penaltiesIncurred.toMutableList()
            val response = PenaltyDistributionModel(
                    displayId = vaultData.creationData.displayId,
                    penalties = penalties
            )
            return response
        }
    }
}