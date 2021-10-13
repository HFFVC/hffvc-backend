package io.agriledger.webserver.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class FieldsToAnonymize(val pathElements: List<String>,
                        val key: String
)