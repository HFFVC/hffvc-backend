package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ProformaInvoiceDTO(val batchId: String,
                         val status: String,
                         val proFormaUpdatedOn: String,
                         val batchProforma_ProformaNo: String,
                         val batchProforma_FullName: String,
                         val batchProforma_Email: String,
                         val batchProforma_Selling_AddressLine1: String,
                         val batchProforma_Selling_AddressLine2: String?,
                         val batchProforma_Selling_AddressLine3: String?,
                         val batchProforma_Shipping_AddressLine1: String,
                         val batchProforma_Shipping_AddressLine2: String?,
                         val batchProforma_Shipping_AddressLine3: String?,
                         val batchProforma_Shipping_UnitPrice: String,
                         val batchProforma_Shipping_Currency: String,
                         val dropLocation: String,
                         val sellerName: String
)