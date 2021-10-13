package io.agriledger.models

import com.fasterxml.jackson.databind.JsonNode

data class TestDataSet (
        val tests: List<TestData>
)

data class TestData(
        val name:String,
        val serviceRequestVaultData: ServiceRequestStateTesting?,
        val batchData: BatchStateTesting?,
        val flowInput: JsonNode,
        val email: String
)