package io.agriledger.utils

import io.agriledger.flows.TestBatchRecordFlow
import io.agriledger.flows.TestServiceRequestRecordFlow
import io.agriledger.models.BatchStateTesting
import io.agriledger.models.ServiceRequestStateTesting
import io.agriledger.models.toBatchStateReflection
import io.agriledger.models.toServiceRequestStateReflection
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode

class VaultDataUtility {
    fun recordServiceRequestDataInVault(sae: StartedMockNode,
                                        lsp: StartedMockNode,
                                        collector: StartedMockNode,
                                        broker: StartedMockNode,
                                        network: MockNetwork,
                                        data: ServiceRequestStateTesting) {

        val serviceRequestVaultDataToRecord = data.toServiceRequestStateReflection(
                sae = sae.info.legalIdentities.first(),
                lsp = lsp.info.legalIdentities.first(),
                collector = collector.info.legalIdentities.first(),
                broker = broker.info.legalIdentities.first(),
                signatories = mutableListOf(sae.info.legalIdentities.first(),
                        lsp.info.legalIdentities.first(),
                        collector.info.legalIdentities.first(),
                        broker.info.legalIdentities.first()).filter { data.signatories.contains(it.name.commonName) }.toMutableList()
        )

        val serviceRequestVaultDataRecordFlow = TestServiceRequestRecordFlow.Initiator(
                serviceRequestVaultDataToRecord
        )

        val serviceRequestVaultDataRecordFlowFuture = sae.startFlow(serviceRequestVaultDataRecordFlow)
        network.runNetwork()
        serviceRequestVaultDataRecordFlowFuture.getOrThrow()
    }

    fun recordBatchDataInVault(sae: StartedMockNode,
                               lsp: StartedMockNode,
                               collector: StartedMockNode,
                               broker: StartedMockNode,
                               network: MockNetwork,
                               data: BatchStateTesting
    ) {

        val batchVaultDataToRecord = data.toBatchStateReflection(
                sae = sae.info.legalIdentities.first(),
                lsp = lsp.info.legalIdentities.first(),
                collector = collector.info.legalIdentities.first(),
                broker = broker.info.legalIdentities.first(),
                signatories = mutableListOf(sae.info.legalIdentities.first(),
                        lsp.info.legalIdentities.first(),
                        collector.info.legalIdentities.first(),
                        broker.info.legalIdentities.first()).filter { data.signatories.contains(it.name.commonName) }.toMutableList()
        )

        val batchVaultDataRecordFlow = TestBatchRecordFlow.Initiator(
                batchVaultDataToRecord
        )

        val batchVaultDataRecordFlowFuture = broker.startFlow(batchVaultDataRecordFlow)
        network.runNetwork()
        batchVaultDataRecordFlowFuture.getOrThrow()
    }
}