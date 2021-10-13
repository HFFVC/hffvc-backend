package io.agriledger.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
class BatchDetailsModel(val batchId: String,
                        val lotIds: MutableList<String> = mutableListOf(),
                        val batchCreatedAt: LocalDateTime,
                        val palletStartQRCode: String,
                        val palletEndQRCode: String,
                        val proformaInvoiceData: ProformaInvoiceModel? = null,
                        val batchUpdateData: UpdateBatchModel? = null,
                        val saleIds: MutableList<String> = mutableListOf(),
                        val signatories: MutableSet<String>,
                        val usernames: MutableSet<String>,
                        val participants: MutableList<String> = mutableListOf()
)