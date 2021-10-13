package io.agriledger.schema

import net.corda.core.schemas.MappedSchema

class SalesSchemaV1 : MappedSchema(SalesSchemaFamily::class.java, 1, listOf(PersistentSalesState::class.java)) {
}