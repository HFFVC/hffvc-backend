package io.agriledger.models

import io.agriledger.model.*
import io.agriledger.states.ServiceRequestState
import net.corda.core.identity.Party
import kotlin.reflect.full.memberProperties

data class ServiceRequestStateTesting(val id: String,
                                      val creationData: ServiceRequestCreationModel,
                                      val acceptData: AcceptServiceRequestModel? = null,
                                      val signatories: MutableList<String> = mutableListOf(),
                                      val usernames: MutableSet<String> = mutableSetOf(),
                                      val enrouteToProducerData: EnrouteToProducerModel? = null,
                                      val withProducerData: WithProducerModel? = null,
                                      val enrouteToPackhouseData: EnrouteToPackhouseModel? = null,
                                      val arrivedAtPackhouseData: ArrivedAtPackhouseModel? = null,
                                      val fruitFlowData: FruitFlowModel? = null,
                                      var batchId: String? = null,
                                      val proformaInvoiceData: ProformaInvoiceModel? = null,
                                      val saleIds: MutableList<LotSaleModel> = mutableListOf(),
                                      val remainingBoxes: Int? = null,
                                      val penaltiesIncurred: MutableList<PenaltyDataModel> = mutableListOf()
)

fun ServiceRequestStateTesting.toServiceRequestStateReflection(
        sae: Party,
        lsp: Party,
        collector: Party,
        broker: Party,
        signatories: MutableList<Party>
) = with(::ServiceRequestState) {
    val propertiesByName = ServiceRequestStateTesting::class.memberProperties.associateBy { it.name }
    callBy(parameters.associate { parameter ->
        parameter to when (parameter.name) {
            ServiceRequestState::sae.name -> sae
            ServiceRequestState::lsp.name -> lsp
            ServiceRequestState::collector.name -> collector
            ServiceRequestState::broker.name -> broker
            ServiceRequestState::signatories.name -> signatories
            ServiceRequestState::participants.name -> listOf(
                    sae, lsp, collector, broker
            )
            else -> propertiesByName[parameter.name]?.get(this@toServiceRequestStateReflection)
        }
    })
}