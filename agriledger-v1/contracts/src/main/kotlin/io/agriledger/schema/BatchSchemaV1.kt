package io.agriledger.schema

import net.corda.core.schemas.MappedSchema

class BatchSchemaV1 : MappedSchema(BatchSchemaFamily::class.java, 1, listOf(PersistentBatchState::class.java)) {
}