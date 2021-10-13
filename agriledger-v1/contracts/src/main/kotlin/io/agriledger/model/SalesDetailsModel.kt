package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class SalesDetailsModel(val saleId: String,
                        val batchId: String,
                        val lotsSold: MutableList<LotSaleModel> = mutableListOf(),
                        val signatories: MutableSet<String>,
                        val usernames: MutableSet<String>,
                        val creationData: NewSaleModel,
                        val shipOrderData: ShipOrderModel? = null,
                        val unloadingAtBuyerData: UnloadingAtBuyerModel? = null,
                        val salesInvoiceData: SalesInvoiceModel? = null,
                        val confirmPaymentData: ConfirmPaymentModel? = null,
                        val participants: MutableList<String>
)