package io.agriledger.flows.utils

import net.corda.core.cordapp.CordappConfig
import net.corda.core.cordapp.CordappConfigException
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub

class NotarySelector {
    fun getNotary(serviceHub: ServiceHub): Party {
        val notaryString = try {
            val config: CordappConfig = serviceHub.getAppContext().config
            config.getString("notary")
        } catch (ex: CordappConfigException) {
            throw FlowException("No notary configured. Please ensure a notary is specified for the cordapp.")
        }
        val notaryX500Name = CordaX500Name.parse(notaryString)
        return serviceHub.networkMapCache.getNotary(notaryX500Name)
            ?: throw FlowException(
                "Notary with the name \"$notaryX500Name\" cannot be found in the network " +
                        "map cache."
            )
    }
}