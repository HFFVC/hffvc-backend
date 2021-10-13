package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EditSalesDetailsDTO(val saleId: String,
                          val totalNoOfBoxesSold: String,
                          val lotId: String,
                          val brokerTransportFlatFee: String,
                          val salesPricePerKg: String,
                          val salesBrokerMargin: String
)