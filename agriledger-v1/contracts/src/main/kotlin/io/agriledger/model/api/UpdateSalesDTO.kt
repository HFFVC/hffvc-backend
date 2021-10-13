package io.agriledger.model.api

import io.agriledger.model.enumerations.SalesStep
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UpdateSalesDTO(val saleId: String,
                     val step: SalesStep,
                     val shipOrder: ShipOrderDTO?,
                     val unloadingAtBuyer: UnloadingAtBuyerPlaceDTO?,
                     val saleInvoice: SaleInvoiceDTO?,
                     val confirmPayment: ConfirmPaymentDTO?
)