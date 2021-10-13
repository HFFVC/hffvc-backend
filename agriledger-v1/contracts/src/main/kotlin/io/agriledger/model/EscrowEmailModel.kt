package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class EscrowEmailModel(
    val brokerName: String?,
    val lspOrganizationName: String?,
    val netPayable: String?,
    val escrowAccountNo: String?
)