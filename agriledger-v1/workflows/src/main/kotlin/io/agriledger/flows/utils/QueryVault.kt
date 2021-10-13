package io.agriledger.flows.utils

import io.agriledger.schema.PersistentBatchState
import io.agriledger.schema.PersistentSalesState
import io.agriledger.schema.PersistentServiceRequestState
import io.agriledger.states.BatchState
import io.agriledger.states.SalesState
import io.agriledger.states.ServiceRequestState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.VaultService
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder

class QueryVault {
    fun queryServiceRequestById(id: String, status: Vault.StateStatus, vaultService: VaultService): StateAndRef<ServiceRequestState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)

        val results = builder {
            val index = PersistentServiceRequestState::id.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.queryBy(ServiceRequestState::class.java, finalCriteria)
        }
        return results.states.singleOrNull()
    }

    fun queryServiceRequestById(id: String, status: Vault.StateStatus, vaultService: CordaRPCOps): StateAndRef<ServiceRequestState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentServiceRequestState::id.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.vaultQueryBy<ServiceRequestState>(finalCriteria)
        }
        return results.states.singleOrNull()
    }

    fun queryServiceRequestByBatch(id: String, status: Vault.StateStatus, vaultService: VaultService): MutableList<StateAndRef<ServiceRequestState>> {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentServiceRequestState::batchId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.queryBy(ServiceRequestState::class.java, finalCriteria)
        }
        return results.states.toMutableList()
    }

    fun queryServiceRequestByBatch(id: String, status: Vault.StateStatus, vaultService: CordaRPCOps): MutableList<StateAndRef<ServiceRequestState>> {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentServiceRequestState::batchId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.vaultQueryBy<ServiceRequestState>(finalCriteria)
        }
        return results.states.toMutableList()
    }

    fun queryBatchById(id: String, status: Vault.StateStatus, vaultService: VaultService): StateAndRef<BatchState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentBatchState::batchId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.queryBy(BatchState::class.java, finalCriteria)
        }
        return results.states.singleOrNull()
    }

    fun queryBatchById(id: String, status: Vault.StateStatus, vaultService: CordaRPCOps): StateAndRef<BatchState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentBatchState::batchId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.vaultQueryBy<BatchState>(finalCriteria)
        }
        return results.states.singleOrNull()
    }

    fun queryBatchesById(id: String, status: Vault.StateStatus, vaultService: VaultService): MutableList<StateAndRef<BatchState>>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentBatchState::batchId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.queryBy(BatchState::class.java, finalCriteria)
        }
        if (results != null && results.states != null && results.states.isNotEmpty()) {
            return results.states.toMutableList()
        } else {
            return null
        }
    }

    fun querySalesById(id: String, status: Vault.StateStatus, vaultService: VaultService): StateAndRef<SalesState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentSalesState::saleId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.queryBy(SalesState::class.java, finalCriteria)
        }
        return results.states.singleOrNull()
    }

    fun querySalesById(id: String, status: Vault.StateStatus, vaultService: CordaRPCOps): StateAndRef<SalesState>? {
        val criteria: QueryCriteria.VaultQueryCriteria = QueryCriteria.VaultQueryCriteria(status = status)
        val results = builder {
            val index = PersistentSalesState::saleId.equal(id)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val finalCriteria = criteria.and(customCriteria)
            vaultService.vaultQueryBy<SalesState>(finalCriteria)
        }
        return results.states.singleOrNull()
    }

}