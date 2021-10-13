package io.agriledger.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.agriledger.flows.utils.QueryVault
import io.agriledger.model.NodeExplorerServiceRequestModel
import io.agriledger.webserver.models.FieldsToAnonymizeDataSet
import io.agriledger.webserver.utils.ExcelHelper
import net.corda.core.node.services.Vault
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import javax.annotation.Resource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import springfox.documentation.swagger.readers.operation.ResponseHeaders.headers
import java.io.ByteArrayInputStream


@CrossOrigin
@RestController
@RequestMapping("/api/v1.0/node-explorer/")
class NodeExplorerController(rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val anonymizationText = "XXXXXXXXXX"

    @GetMapping(value = ["templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping(value = ["search-by-batch"], produces = [APPLICATION_JSON_VALUE])
    fun searchBatch(batchId: String, @ApiIgnore authentication: Authentication): ResponseEntity<List<NodeExplorerServiceRequestModel>> {
        val wantedBatch = QueryVault().queryBatchById(batchId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Batch $batchId not found.")
        val batchVaultData = wantedBatch.state.data

        val associatedServiceRequests = QueryVault().queryServiceRequestByBatch(batchId, Vault.StateStatus.UNCONSUMED, proxy)
        val associatedLots = mutableListOf<NodeExplorerServiceRequestModel>()
        associatedServiceRequests.forEach {
            val serviceRequestVault = it.state.data
            associatedLots.add(NodeExplorerServiceRequestModel(
                    id = serviceRequestVault.id,
                    createdOn = serviceRequestVault.creationData.requestedOn
            ))
        }

        return ResponseEntity.ok(associatedLots)
    }

    @GetMapping(value = ["search-by-sale"], produces = [APPLICATION_JSON_VALUE])
    fun searchSale(saleId: String, @ApiIgnore authentication: Authentication): ResponseEntity<List<NodeExplorerServiceRequestModel>> {
        val wantedSale = QueryVault().querySalesById(saleId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Sale $saleId not found.")
        val salesVaultData = wantedSale.state.data
        val associatedLots = mutableListOf<NodeExplorerServiceRequestModel>()
        salesVaultData.lotsSold.forEach {
            val wantedState = QueryVault().queryServiceRequestById(it.id, Vault.StateStatus.UNCONSUMED, proxy)
                    ?: throw Exception("Service Request ${it.id} not found")
            val serviceRequest = wantedState.state.data
            associatedLots.add(NodeExplorerServiceRequestModel(
                    id = it.id,
                    createdOn = serviceRequest.creationData.requestedOn
            ))
        }

        return ResponseEntity.ok(associatedLots)
    }

    @GetMapping(value = ["service-request-details"], produces = [APPLICATION_JSON_VALUE])
    fun getServiceRequestDetails(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<Map<String, Any>> {

        val wantedServiceRequest = QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Service Request $id not found.")

        val vaultData = wantedServiceRequest.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "SERVICE_REQUEST_STATE" }

        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.remove(it.key)
        }

        return ResponseEntity.ok(json.toMap())
    }

    @GetMapping(value = ["batch-details"], produces = [APPLICATION_JSON_VALUE])
    fun getBatchDetails(batchId: String, @ApiIgnore authentication: Authentication): ResponseEntity<Map<String, Any>> {

        val wantedBatch = QueryVault().queryBatchById(batchId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Batch $batchId not found.")

        val vaultData = wantedBatch.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "BATCH_STATE" }


        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.remove(it.key)
        }
        return ResponseEntity.ok(json.toMap())
    }

    @GetMapping(value = ["sales-details"], produces = [APPLICATION_JSON_VALUE])
    fun getSalesDetails(saleId: String, @ApiIgnore authentication: Authentication): ResponseEntity<Map<String, Any>> {

        val wantedSale = QueryVault().querySalesById(saleId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Sale $saleId not found.")

        val vaultData = wantedSale.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "SALE_STATE" }

        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.remove(it.key)
        }
        return ResponseEntity.ok(json.toMap())
    }

    @GetMapping(value = ["download-service-request"])
    fun downloadServiceRequest(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<InputStreamResource> {
        val wantedServiceRequest = QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Service Request $id not found.")

        val vaultData = wantedServiceRequest.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "SERVICE_REQUEST_STATE" }

        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.put(it.key, anonymizationText)
        }


        val filename = "Service_Request_$id.xlsx"
        val file = InputStreamResource(ExcelHelper().generateExcelFromData(json, "Service Request: Vault Details"))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file)
    }

    @GetMapping(value = ["download-batch-details"])
    fun downloadBatchDetails(batchId: String, @ApiIgnore authentication: Authentication): ResponseEntity<InputStreamResource> {
        val wantedBatch = QueryVault().queryBatchById(batchId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Batch $batchId not found.")

        val vaultData = wantedBatch.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "BATCH_STATE" }

        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.put(it.key, anonymizationText)
        }


        val filename = "batch_$batchId.xlsx"
        val file = InputStreamResource(ExcelHelper().generateExcelFromData(json, "Batch: Vault Details"))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file)
    }

    @GetMapping(value = ["download-sale-details"])
    fun downloadSaleDetails(saleId: String, @ApiIgnore authentication: Authentication): ResponseEntity<InputStreamResource> {
        val wantedSale = QueryVault().querySalesById(saleId, Vault.StateStatus.UNCONSUMED, proxy)
                ?: throw Exception("Sale $saleId not found.")

        val vaultData = wantedSale.state.data

        val json = JSONObject(vaultData)
        val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

        val dataSet: FieldsToAnonymizeDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("fields-to-anonymize.json"))

        val fields = dataSet.fields.single { it.state == "SALE_STATE" }

        fields.fieldsToAnonymize.forEach {
            var jsonInContext = json
            var isPathPresent = true
            for (pathElement in it.pathElements) {
                if (jsonInContext.has(pathElement))
                    jsonInContext = jsonInContext.getJSONObject(pathElement)
                else {
                    isPathPresent = false
                    break
                }
            }
            if (isPathPresent)
                jsonInContext.put(it.key, anonymizationText)
        }


        val filename = "sale_$saleId.xlsx"
        val file = InputStreamResource(ExcelHelper().generateExcelFromData(json, "Sale: Vault Details"))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file)
    }
}