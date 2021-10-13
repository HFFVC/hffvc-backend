package io.agriledger.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class ListOfServiceRequests(val serviceRequests: MutableList<ServiceRequestData>)