package io.agriledger.states

import io.agriledger.contracts.BatchContract
import io.agriledger.model.ProformaInvoiceModel
import io.agriledger.model.UpdateBatchModel
import io.agriledger.schema.BatchSchemaV1
import io.agriledger.schema.PersistentBatchState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.time.LocalDateTime

// *********
// * State *
// *********
@BelongsToContract(BatchContract::class)
data class BatchState(val batchId: String,
                      val sae: Party,
                      val lsp: Party,
                      val collector: Party,
                      val broker: Party,
                      val batchCreatedAt: LocalDateTime,
                      val palletStartQRCode: String,
                      val palletEndQRCode: String,
                      val proformaInvoiceData: ProformaInvoiceModel? = null,
                      val batchUpdateData: UpdateBatchModel? = null,
                      val saleIds: MutableList<String> = mutableListOf(),
                      val signatories: MutableList<Party>,
                      val usernames: MutableSet<String>,
                      override val participants: List<AbstractParty> = listOf(
                              sae, lsp, collector, broker
                      )
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is BatchSchemaV1) {
            return PersistentBatchState(
                    this.batchId
            )
        } else {
            throw IllegalArgumentException("Unsupported Schema");
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(BatchSchemaV1())
    }
}

