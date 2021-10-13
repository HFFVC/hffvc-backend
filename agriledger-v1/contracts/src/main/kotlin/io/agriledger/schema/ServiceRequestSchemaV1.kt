package io.agriledger.schema

import net.corda.core.schemas.MappedSchema

class ServiceRequestSchemaV1 : MappedSchema(ServiceRequestSchemaFamily::class.java, 1, listOf(PersistentServiceRequestState::class.java))
