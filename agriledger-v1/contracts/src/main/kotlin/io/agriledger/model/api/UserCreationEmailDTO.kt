package io.agriledger.model.api

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class UserCreationEmailDTO(val userFullName: String, val emailId: String, val password: String)