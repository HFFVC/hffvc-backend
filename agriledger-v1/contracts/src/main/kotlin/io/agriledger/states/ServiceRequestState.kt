package io.agriledger.states

import io.agriledger.contracts.ServiceRequestContract
import io.agriledger.model.*
import io.agriledger.schema.PersistentServiceRequestState
import io.agriledger.schema.ServiceRequestSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
@BelongsToContract(ServiceRequestContract::class)
data class ServiceRequestState(val id: String,
                               val sae: Party,
                               val lsp: Party,
                               val collector: Party,
                               val broker: Party,
                               val creationData: ServiceRequestCreationModel,
                               val acceptData: AcceptServiceRequestModel? = null,
                               val signatories: MutableList<Party>,
                               val usernames: MutableSet<String>,
                               val lotRejectData: RejectLotModel? = null,
                               val enrouteToProducerData: EnrouteToProducerModel? = null,
                               val withProducerData: WithProducerModel? = null,
                               val enrouteToPackhouseData: EnrouteToPackhouseModel? = null,
                               val arrivedAtPackhouseData: ArrivedAtPackhouseModel? = null,
                               val fruitFlowData: FruitFlowModel? = null,
                               var batchId: String? = null,
                               val proformaInvoiceData: ProformaInvoiceModel? = null,
                               val saleIds: MutableList<LotSaleModel> = mutableListOf(),
                               val remainingBoxes: Int? = null,
                               val penaltiesIncurred: MutableList<PenaltyDataModel> = mutableListOf(),
                               val firstSale: String? = null,
                               override val participants: List<AbstractParty> = listOf(
                                       sae, lsp, collector, broker
                               )
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is ServiceRequestSchemaV1) {
            return PersistentServiceRequestState(
                    this.id, this.batchId, this.creationData.requestedOn
            )
        } else {
            throw IllegalArgumentException("Unsupported Schema");
        }
    }


    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(ServiceRequestSchemaV1())
    }
}

