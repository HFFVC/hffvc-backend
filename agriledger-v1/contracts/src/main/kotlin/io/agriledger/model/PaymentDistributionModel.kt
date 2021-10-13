package io.agriledger.model

import io.agriledger.model.enumerations.Product
import io.agriledger.model.enumerations.Shipping
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class PaymentDistributionModel(val saleId: String,
                               val soldOn: LocalDateTime? = null,
                               val netSales: Float,
                               val product: Product,
                               val totalBoxesSold: Int,
                               val totalWeightOfFruitsSold: Float,
                               val pricePerKg: Float,
                               val shipmentMode: Shipping,
                               val wasFactored: Boolean? = null,
                               val factoringEntity: String? = null,
                               val factoringCharges: Float? = null,
                               val factoringAmount: Float? = null,
                               val factoringAgent: String? = null,
                               val brokerMargin: Float? = null,
                               val brokerTransport: Float? = null,
                               val bankTransactionCharges: Float? = null,
                               val sales: MutableList<SalesDistributionModel>,
                               val lots: MutableList<LotDistributionModel>
)