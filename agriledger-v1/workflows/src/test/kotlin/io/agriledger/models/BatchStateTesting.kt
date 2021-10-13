package io.agriledger.models

import io.agriledger.model.*
import io.agriledger.states.BatchState
import net.corda.core.identity.Party
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class BatchStateTesting(val batchId: String,
                             val batchCreatedAt: LocalDateTime,
                             val palletStartQRCode: String,
                             val palletEndQRCode: String,
                             val proformaInvoiceData: ProformaInvoiceModel? = null,
                             val batchUpdateData: UpdateBatchModel? = null,
                             val saleIds: MutableList<String> = mutableListOf(),
                             val signatories: MutableList<String> = mutableListOf(),
                             val usernames: MutableSet<String> = mutableSetOf()
)

fun BatchStateTesting.toBatchStateReflection(
        sae: Party,
        lsp: Party,
        collector: Party,
        broker: Party,
        signatories: MutableList<Party>
) = with(::BatchState) {
    val propertiesByName = BatchStateTesting::class.memberProperties.associateBy { it.name }
    callBy(parameters.associate { parameter ->
        parameter to when (parameter.name) {
            BatchState::sae.name -> sae
            BatchState::lsp.name -> lsp
            BatchState::collector.name -> collector
            BatchState::broker.name -> broker
            BatchState::signatories.name -> signatories
            BatchState::participants.name -> listOf(
                    sae, lsp, collector, broker
            )
            else -> propertiesByName[parameter.name]?.get(this@toBatchStateReflection)
        }
    })
}