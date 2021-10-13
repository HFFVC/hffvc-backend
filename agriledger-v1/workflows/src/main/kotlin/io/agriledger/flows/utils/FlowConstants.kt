package io.agriledger.flows.utils

import net.corda.core.identity.CordaX500Name

object FlowConstants {
    val saeName = CordaX500Name(organisation = "SAE", locality = "Port-au-Prince", country = "HT")
    val lspName = CordaX500Name(organisation = "LSP", locality = "Port-au-Prince", country = "HT")
    val collectorName = CordaX500Name(organisation = "Collector", locality = "Port-au-Prince", country = "HT")
    val brokerName = CordaX500Name(organisation = "Broker", locality = "New York", country = "US")
}