package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ServiceRequestDetailsModel(val id: String,
                                 val creationData: ServiceRequestCreationModel,
                                 val acceptData: AcceptServiceRequestModel? = null,
                                 val signatories: MutableSet<String> = mutableSetOf<String>(),
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
                                 val participants: MutableList<String> = mutableListOf()
)