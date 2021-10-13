package io.agriledger.states

import io.agriledger.contracts.SalesContract
import io.agriledger.model.*
import io.agriledger.schema.PersistentSalesState
import io.agriledger.schema.SalesSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
@BelongsToContract(SalesContract::class)
data class SalesState(
    val saleId: String,
    val batchId: String,
    val lotsSold: MutableList<LotSaleModel> = mutableListOf(),
    val sae: Party,
    val lsp: Party,
    val collector: Party,
    val broker: Party,
    val signatories: MutableList<Party>,
    val usernames: MutableSet<String>,
    val creationData: NewSaleModel,
    val shipOrderData: ShipOrderModel? = null,
    val unloadingAtBuyerData: UnloadingAtBuyerModel? = null,
    val salesInvoiceData: SalesInvoiceModel? = null,
    val confirmPaymentData: ConfirmPaymentModel? = null,
    val flaggedTransaction: Boolean? = null,
    val paymentDistribution: PaymentDistributionModel? = null,
    override val participants: List<AbstractParty> = listOf(
        sae, lsp, collector, broker
    )
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is SalesSchemaV1) {
            return PersistentSalesState(
                this.saleId,
                this.flaggedTransaction
            )
        } else {
            throw IllegalArgumentException("Unsupported Schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(SalesSchemaV1())
    }
}

