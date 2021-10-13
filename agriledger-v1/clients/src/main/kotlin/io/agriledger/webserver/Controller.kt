package io.agriledger.webserver

import com.sendgrid.helpers.mail.objects.Attachments
import io.agriledger.flows.CollectorArrivedAtPackHouseFlow.Initiator as CollectorArrivedAtPackHouseFlowInitiator
import io.agriledger.flows.CollectorEnRouteToProducerFlow.Initiator as CollectorEnRouteToProducerFlowInitiator
import io.agriledger.flows.RejectLotFlow.Initiator as RejectLotFlowInitiator
import io.agriledger.flows.WithProducerFlow.Initiator as CollectorWithProducerFlowInitiator
import io.agriledger.flows.CollectorEnrouteToPackhouseFlow.Initiator as CollectorEnrouteToPackhouseFlowInitiator
import io.agriledger.flows.FruitFlow.Initiator as FruitFlowInitiator
import io.agriledger.flows.CreateBatchFlow.Initiator as CreateBatchInitiator
import io.agriledger.flows.UpdateBatch.Initiator as UpdateBatchFlowInitiator
import io.agriledger.flows.NewSaleFlow.Initiator as NewSaleFlowInitiator
import io.agriledger.flows.ProformaInvoiceCreationFlow.Initiator as ProformaInvoiceCreationFlowInitiator
import io.agriledger.flows.UpdateSalesFlow.Initiator as UpdateSalesInitiator
import io.agriledger.flows.AcceptServiceRequestFlow.Initiator as AcceptServiceRequestFlowInitiator
import io.agriledger.flows.PenaltiesCalculationFlow.Initiator as PenaltyCalculatorFlowInitiator
import io.agriledger.flows.CreateServiceRequestFlow.Initiator
import io.agriledger.flows.NoSaleFlow.Initiator as NoFlowInitiator
import io.agriledger.flows.PaymentDist.Distribution as PaymentDistribution
import io.agriledger.flows.PenaltyDistribution.Distribution as PenaltyDistribution
import io.agriledger.flows.DeleteDuplicateBatchFlow.Initiator as DeleteBatch
import io.agriledger.flows.DeleteServiceRequestFlow.Initiator as DeleteServiceRequest
import io.agriledger.flows.UndoSaleFlow.Initiator as UndoSaleInitiator
import io.agriledger.flows.EditSalesDetailsFlow.Initiator as EditSalesDetailsInitiator
import io.agriledger.flows.DeletePenaltiesFlow.Initiator as DeletePenaltiesInitiator
import io.agriledger.flows.EditServiceRequestDetailsFlow.Initiator as EditServiceRequestInitiator
import io.agriledger.flows.UpdateIdentificationNoFlow.Initiator as UpdateIdentificationNoInitiator
import io.agriledger.flows.PaymentDistributionFlow.Initiator as PaymentDistributionFlowInitiator
import io.agriledger.flows.utils.QueryVault
import io.agriledger.flows.utils.ValidateFruitFlowStages
import io.agriledger.model.*
import io.agriledger.webserver.models.EmailContentsModel
import io.agriledger.webserver.models.EmailDataModel
import io.agriledger.model.api.*
import io.agriledger.model.enumerations.SalesStep
import io.agriledger.schema.PersistentSalesState
import io.agriledger.schema.PersistentServiceRequestState
import io.agriledger.states.SalesState
import io.agriledger.states.ServiceRequestState
import io.agriledger.webserver.utils.HtmlFormatter
import io.agriledger.webserver.models.User
import io.agriledger.webserver.utils.ConfigConstants
import io.agriledger.webserver.utils.FileGenerator
import io.agriledger.webserver.utils.SendEmail
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.*
import net.corda.core.node.services.vault.Builder.greaterThanOrEqual
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDateTime
import kotlin.Exception


/**
 * Define your API endpoints here.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/v1.0/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    /**
     * Initiates a flow to create a service request.
     *
     * Once the flow finishes it will have written the Service Request to ledger.

     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @PostMapping(
        value = ["create-servicerequest"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun createServiceRequest(
        @RequestBody request: ServiceRequestCreationDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }


    /**
     * Fetches a Service Request by Id.
     */
    @GetMapping(value = ["service-request-by-id"], produces = [APPLICATION_JSON_VALUE])
    fun getServiceRequests(
        id: String,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<ServiceRequestDataModel>? {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email

        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            if (!ConfigConstants.configData.nodeExplorerUsers.contains(email)) {
                throw Exception("User does not have sufficient permissions.")
            }
        }
        val result = QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, proxy)
        if (result != null) {
            val vaultData = result.state.data
            val participants = mutableListOf<String>()
            vaultData.participants.forEach {
                it.nameOrNull()?.organisation?.let { name -> participants.add(name) }
            }
            val signatories = mutableSetOf<String>()
            vaultData.signatories.forEach {
                signatories.add(it.name.organisation)
            }
            val listOfSales = mutableListOf<String>()
            vaultData.saleIds.forEach {
                listOfSales.add(it.id)
            }
            val response = ServiceRequestDataModel(
                id = vaultData.id,
                participants = participants,
                signatories = signatories,
                txHash = result.ref.txhash.toString(),
                displayId = vaultData.creationData.displayId,
                status = vaultData.creationData.status,
                producerId = vaultData.creationData.farmerId,
                location = vaultData.creationData.location,
                department = vaultData.creationData.department,
                town = vaultData.creationData.town,
                requestedOn = vaultData.creationData.requestedOn,
                estimatedNoOfFruits = vaultData.creationData.estimatedNoFruits,
                product = vaultData.creationData.product.toString(),
                collector = vaultData.acceptData?.collectorName,
                collectionPoint = vaultData.acceptData?.collectionPoint,
                batchId = vaultData.batchId,
                saleIds = listOfSales,
                remainingBoxes = vaultData.remainingBoxes
            )
            return ResponseEntity.ok(response)
        }
        return null
    }

    /**
     * Getting service requests
     */
    @GetMapping(value = ["service-requests-recent"], produces = [APPLICATION_JSON_VALUE])
    fun getServiceRequests(@ApiIgnore authentication: Authentication): ResponseEntity<ListOfServiceRequests> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            if (!ConfigConstants.configData.nodeExplorerUsers.contains(email)) {
                throw Exception("User does not have sufficient permissions.")
            }
        }

        val queryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val period = LocalDateTime.now().minusDays(30)
        println(period)
        val dateFilter = PersistentServiceRequestState::createdOn.greaterThanOrEqual(period)
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateFilter)
        val finalCriteria = queryCriteria.and(customCriteria)
        val sortColumn = Sort.SortColumn(
            SortAttribute.Custom(PersistentServiceRequestState::class.java, "createdOn"),
            Sort.Direction.DESC
        )
        val sortList = mutableListOf(sortColumn)
        val results = proxy.vaultQueryBy<ServiceRequestState>(
            finalCriteria,
            PageSpecification(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE),
            Sort(sortList)
        )
        val filtered = mutableListOf<ServiceRequestData>()
        results.states.forEach {
            val vaultData = it.state.data
            filtered.add(
                ServiceRequestData(
                    id = vaultData.id,
                    displayId = vaultData.creationData.displayId,
                    createdOn = vaultData.creationData.requestedOn
                )
            )
        }
        return ResponseEntity.ok(ListOfServiceRequests(serviceRequests = filtered))
    }

    /**
     * Fetches a Sales by Id.
     */
    @GetMapping(value = ["sale-by-id"], produces = [APPLICATION_JSON_VALUE])
    fun getSaleById(id: String): ResponseEntity<StateAndRef<SalesState>> {
        return ResponseEntity.ok(QueryVault().querySalesById(id, Vault.StateStatus.UNCONSUMED, proxy))
    }

    @PostMapping(
        value = ["collector-en-route-to-producer"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun collectorEnRouteToProducer(
        @RequestBody request: EnrouteToProducerDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(
                ::CollectorEnRouteToProducerFlowInitiator,
                request,
                email
            ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["accept-service-request"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun acceptServiceRequest(
        @RequestBody request: AcceptServiceRequestDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx =
                proxy.startTrackedFlow(::AcceptServiceRequestFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    // rejecting a lot
    @PostMapping(
        value = ["reject-lot"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun collectorRejectLot(
        @RequestBody request: RejectLotDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::RejectLotFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["with-producer"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun collectorWithProducer(
        @RequestBody request: WithProducerDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx =
                proxy.startTrackedFlow(::CollectorWithProducerFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["collector-enroute-packhouse"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun collectorEnroutePackhouse(
        @RequestBody request: EnrouteToPackhouseDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(
                ::CollectorEnrouteToPackhouseFlowInitiator,
                request,
                email
            ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["collector-arrived-at-packhouse"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun collectorArrivedAtPackHouse(
        @RequestBody request: ArrivedAtPackHouseDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(
                ::CollectorArrivedAtPackHouseFlowInitiator,
                request,
                email
            ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["fruit-flow"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun fruitFlow(
        @RequestBody request: FruitFlowDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::FruitFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["create-batch"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun createBatch(@RequestBody request: BatchDTO, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::CreateBatchInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["proforma-invoice"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun proformaInvoice(
        @RequestBody request: ProformaInvoiceDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx =
                proxy.startTrackedFlow(::ProformaInvoiceCreationFlowInitiator, request, email).returnValue.getOrThrow()

            val wantedStateRef =
                QueryVault().queryServiceRequestByBatch(request.batchId, Vault.StateStatus.UNCONSUMED, proxy).first()
            val vaultData = wantedStateRef.state.data

            val wantedBatch = QueryVault().queryBatchById(request.batchId, Vault.StateStatus.UNCONSUMED, proxy)
            val batchVault = wantedBatch?.state?.data

            if (wantedBatch != null) {
                val emailData = batchVault?.proformaInvoiceData?.let {
                    EmailContentsModel(
                        invoiceNo = it.batchProforma_ProformaNo,
                        sellerName = it.sellerName,
                        dropLocation = it.dropLocation,
                        product = vaultData.creationData.product,
                        unitPrice = it.batchProforma_Shipping_UnitPrice,
                        proformaInvoiceCurrency = it.batchProforma_Shipping_Currency,
                        fullName = it.batchProforma_FullName,
                        email = it.batchProforma_Email,
                        sellingAddressLine1 = it.batchProforma_Shipping_AddressLine1,
                        sellingAddressLine2 = it.batchProforma_Shipping_AddressLine2,
                        sellingAddressLine3 = it.batchProforma_Selling_AddressLine3,
                        shippingAddressLine1 = it.batchProforma_Shipping_AddressLine1,
                        shippingAddressLine2 = it.batchProforma_Shipping_AddressLine2,
                        shippingAddressLine3 = it.batchProforma_Shipping_AddressLine3
                    )
                }
                if (emailData != null) {
                    val emailObj = SendEmail()
                    val htmlFormatter = HtmlFormatter()
                    val fileGenerator = FileGenerator()
                    val toAddress = emailData.email ?: throw Exception("Error finding email id")
                    val to = mutableSetOf(toAddress)
                    val emailTemplate = htmlFormatter.proformaInvoiceMailTemplate(emailData)
                    val invoiceTemplate = htmlFormatter.proformaInvoice(emailData)
                    if (to != null && emailData.invoiceNo != null) {
                        emailObj.sendEmail(
                            EmailDataModel(
                                from = ConfigConstants.configData.supportEmail,
                                fromName = ConfigConstants.configData.supportEmailFrom,
                                to = to,
                                subject = "AgriLedger: Your Invoice ${emailData.invoiceNo!!}",
                                emailTemplate = emailTemplate,
                                attachments = mutableListOf(
                                    emailObj.generateEmailAttachment(
                                        fileGenerator.convertHtmlToPdf(invoiceTemplate),
                                        "invoice_${emailData.invoiceNo}.pdf",
                                        "application/pdf"
                                    )
                                )
                            )
                        )
                    }
                }
            } else {
                throw Exception("Batch not found")
            }

            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["update-batch"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun updateBatch(
        @RequestBody request: UpdateBatchDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::UpdateBatchFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["new-sale"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun createSale(
        @RequestBody request: NewSaleDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::NewSaleFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["update-sale"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun createSale(
        @RequestBody request: UpdateSalesDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::UpdateSalesInitiator, request, email).returnValue.getOrThrow()
            if (request.step == SalesStep.SALE_INVOICE) {
                val wantedSaleState = QueryVault().querySalesById(request.saleId, Vault.StateStatus.UNCONSUMED, proxy)
                if (wantedSaleState != null) {
                    val vaultData = wantedSaleState.state.data
                    val wantedBatchState =
                        QueryVault().queryBatchById(vaultData.batchId, Vault.StateStatus.UNCONSUMED, proxy)
                            ?: throw Exception("Batch ${vaultData.batchId} not found.")

                    val batchVault = wantedBatchState.state.data
                    val brokerEmailId = batchVault.proformaInvoiceData?.batchProforma_Email
                        ?: throw Exception("Broker email id not found")

                    val emailData = vaultData.salesInvoiceData?.let {
                        EmailContentsModel(
                            saleId = vaultData.saleId,
                            product = vaultData.creationData.soldProduct,
                            pricePerKg = it.pricePerKg,
                            netSales = it.netSales,
                            aproximateWeightOfProduct = it.aproximateWeightOfProduct,
                            GICurrency = it.GICurrency,
                            pricePerKgCurrency = it.pricePerKgCurrency,
                            buyerOrganization = it.buyerOrganization,
                            buyerName = it.buyerName,
                            brokerOrganization = it.brokerOrganization,
                            brokerName = it.brokerName,
                            billingDate = it.billingDate,
                            invoiceNo = it.invoiceNo,
                            boxesSold = it.noofBoxesPurchased,
                            emailId = brokerEmailId
                        )
                    }
                    if (emailData != null) {
                        val toAddress = emailData.emailId ?: throw Exception("Error finding email id")
                        val to = mutableSetOf(toAddress)
                        val emailObj = SendEmail()
                        val htmlFormatter = HtmlFormatter()
                        val fileGenerator = FileGenerator()
                        val emailTemplate = htmlFormatter.brokerInvoiceMailTemplate(emailData)
                        val invoiceTemplate = htmlFormatter.brokerInvoice(emailData)
                        if (to != null && emailData.invoiceNo != null) {
                            emailObj.sendEmail(
                                EmailDataModel(
                                    from = ConfigConstants.configData.supportEmail,
                                    fromName = ConfigConstants.configData.supportEmailFrom,
                                    to = to,
                                    subject = "${emailData.brokerOrganization}: Your Invoice ${emailData.invoiceNo!!}",
                                    emailTemplate = emailTemplate,
                                    attachments = mutableListOf(
                                        emailObj.generateEmailAttachment(
                                            fileGenerator.convertHtmlToPdf(invoiceTemplate),
                                            "invoice_${emailData.invoiceNo}.pdf",
                                            "application/pdf"
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            } else if (request.step == SalesStep.CONFIRM_PAYMENT) {
                val wantedSaleState = QueryVault().querySalesById(request.saleId, Vault.StateStatus.UNCONSUMED, proxy)
                if (wantedSaleState != null) {
                    val emailObj = SendEmail()
                    val htmlFormatter = HtmlFormatter()

                    val vaultData = wantedSaleState.state.data
                    // getting broker email id from proforma invoice data
                    val batchId = vaultData.batchId
                    val wantedBatch = QueryVault().queryBatchById(batchId, Vault.StateStatus.UNCONSUMED, proxy)
                        ?: throw Exception("Batch not found")
                    val batchVault = wantedBatch.state.data
                    val toAddress = batchVault.proformaInvoiceData?.batchProforma_Email
                        ?: throw Exception("Broker email id not found")
                    val to = mutableSetOf<String>()
                    to.add(toAddress)
                    val emailData = EscrowEmailModel(
                        brokerName = batchVault.proformaInvoiceData?.batchProforma_FullName,
                        lspOrganizationName = vaultData.creationData.lspOrganizationName,
                        netPayable = request.confirmPayment?.netPayable,
                        escrowAccountNo = request.confirmPayment?.escrowAccountNo
                    )
                    val emailTemplate = htmlFormatter.escrowAccountTemplate(emailData)

                    emailObj.sendEmail(
                        EmailDataModel(
                            from = ConfigConstants.configData.supportEmail,
                            fromName = ConfigConstants.configData.supportEmailFrom,
                            to = to,
                            subject = "Escrow Account Details",
                            emailTemplate = emailTemplate
                        )
                    )
                }
            }
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["penalties"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun calculatePenalties(
        @RequestBody request: PenaltiesDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx =
                proxy.startTrackedFlow(::PenaltyCalculatorFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["no-sale"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun noSale(@RequestBody request: NoSaleDTO, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx = proxy.startTrackedFlow(::NoFlowInitiator, request, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["payment-distribution"],
        produces = [TEXT_PLAIN_VALUE],
        headers = ["Content-Type=application/json"]
    )
    fun computePaymentDistribution(saleId: String, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        return try {
            val signedTx =
                proxy.startTrackedFlow(::PaymentDistributionFlowInitiator, saleId, email).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @GetMapping(
        value = ["payment-distribution"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun getPaymentDistribution(
        saleId: String,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<PaymentDistributionModel> {
        return ResponseEntity.ok(PaymentDistribution(saleId).getDistribution(proxy))
    }

    @GetMapping(
        value = ["penalties"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun penaltyDistribution(
        serviceRequestId: String,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<PenaltyDistributionModel> {
        return ResponseEntity.ok(PenaltyDistribution(serviceRequestId, proxy).calculateDistribution())
    }

    @GetMapping(
        value = ["service-request-details"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun getServiceRequestDetails(
        id: String,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<ServiceRequestDetailsModel>? {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        val result = QueryVault().queryServiceRequestById(id, Vault.StateStatus.UNCONSUMED, proxy)
        if (result != null) {
            val vaultData = result.state.data
            val participants = mutableListOf<String>()
            vaultData.participants.forEach {
                it.nameOrNull()?.organisation?.let { name -> participants.add(name) }
            }
            val signatories = mutableSetOf<String>()
            vaultData.signatories.forEach {
                signatories.add(it.name.organisation)
            }
            val listOfSales = mutableListOf<String>()
            vaultData.saleIds.forEach {
                listOfSales.add(it.id)
            }
            val response = ServiceRequestDetailsModel(
                id = vaultData.id,
                participants = participants,
                signatories = signatories,
                creationData = vaultData.creationData,
                acceptData = vaultData.acceptData,
                usernames = vaultData.usernames,
                lotRejectData = vaultData.lotRejectData,
                enrouteToProducerData = vaultData.enrouteToProducerData,
                withProducerData = vaultData.withProducerData,
                enrouteToPackhouseData = vaultData.enrouteToPackhouseData,
                arrivedAtPackhouseData = vaultData.arrivedAtPackhouseData,
                fruitFlowData = vaultData.fruitFlowData,
                batchId = vaultData.batchId,
                proformaInvoiceData = vaultData.proformaInvoiceData,
                saleIds = vaultData.saleIds,
                remainingBoxes = vaultData.remainingBoxes,
                penaltiesIncurred = vaultData.penaltiesIncurred
            )
            return ResponseEntity.ok(response)
        }
        return null
    }

    @GetMapping(
        value = ["batch-details"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun getBatchDetails(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<BatchDetailsModel>? {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        val result = QueryVault().queryBatchById(id, Vault.StateStatus.UNCONSUMED, proxy)
        if (result != null) {
            val allServiceRequests = QueryVault().queryServiceRequestByBatch(id, Vault.StateStatus.UNCONSUMED, proxy)
            val lotIds = mutableListOf<String>()
            allServiceRequests.forEach {
                lotIds.add(it.state.data.id)
            }
            val vaultData = result.state.data
            val participants = mutableListOf<String>()
            vaultData.participants.forEach {
                it.nameOrNull()?.organisation?.let { name -> participants.add(name) }
            }
            val signatories = mutableSetOf<String>()
            vaultData.signatories.forEach {
                signatories.add(it.name.organisation)
            }
            val response = BatchDetailsModel(
                batchId = vaultData.batchId,
                lotIds = lotIds,
                batchCreatedAt = vaultData.batchCreatedAt,
                palletStartQRCode = vaultData.palletStartQRCode,
                palletEndQRCode = vaultData.palletEndQRCode,
                proformaInvoiceData = vaultData.proformaInvoiceData,
                batchUpdateData = vaultData.batchUpdateData,
                saleIds = vaultData.saleIds,
                signatories = signatories,
                usernames = vaultData.usernames,
                participants = participants
            )
            return ResponseEntity.ok(response)
        }
        return null
    }

    @GetMapping(
        value = ["sale-details"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun getSalesDetails(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<SalesDetailsModel>? {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        val result = QueryVault().querySalesById(id, Vault.StateStatus.UNCONSUMED, proxy)
        if (result != null) {
            val vaultData = result.state.data
            val participants = mutableListOf<String>()
            vaultData.participants.forEach {
                it.nameOrNull()?.organisation?.let { name -> participants.add(name) }
            }
            val signatories = mutableSetOf<String>()
            vaultData.signatories.forEach {
                signatories.add(it.name.organisation)
            }
            val response = SalesDetailsModel(
                saleId = vaultData.saleId,
                batchId = vaultData.batchId,
                lotsSold = vaultData.lotsSold,
                signatories = signatories,
                usernames = vaultData.usernames,
                creationData = vaultData.creationData,
                shipOrderData = vaultData.shipOrderData,
                unloadingAtBuyerData = vaultData.unloadingAtBuyerData,
                salesInvoiceData = vaultData.salesInvoiceData,
                confirmPaymentData = vaultData.confirmPaymentData,
                participants = participants
            )
            return ResponseEntity.ok(response)
        }
        return null
    }

    @DeleteMapping(
        value = ["delete-batch"],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun deleteDuplicateBatch(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        return try {
            val signedTx = proxy.startTrackedFlow(::DeleteBatch, id).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction ${signedTx.id} successful \n Batch $id deleted.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = ["undo-sale"], produces = [TEXT_PLAIN_VALUE])
    fun undoSale(@RequestBody request: UndoSaleDTO, authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        return try {
            val signedTx = proxy.startTrackedFlow(::UndoSaleInitiator, request).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Sale for service request id ${request.lotId} reverted.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = ["edit-sales-details"], produces = [TEXT_PLAIN_VALUE])
    fun editSaleDetails(
        @RequestBody request: EditSalesDetailsDTO,
        authentication: Authentication
    ): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        return try {
            val signedTx = proxy.startTrackedFlow(::EditSalesDetailsInitiator, request).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @DeleteMapping(
        value = ["delete-penalties"],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun deletePenalties(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        return try {
            val signedTx = proxy.startTrackedFlow(::DeletePenaltiesInitiator, id).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["edit-service-request"],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun editServiceRequestDetails(
        @RequestBody request: EditServiceRequestDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        return try {
            val signedTx = proxy.startTrackedFlow(::EditServiceRequestInitiator, request).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["update-identificationno-details"],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun updateIdentificationNoDetails(
        @RequestBody request: UpdateIdentificationNoDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        return try {
            val signedTx = proxy.startTrackedFlow(::UpdateIdentificationNoInitiator, request).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction id ${signedTx.id} committed to ledger.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @GetMapping(
        value = ["validate-fruit-flow"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun validateFruitFlow(
        @RequestBody req: ValidateFruitFlowDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String>? {
        val result = QueryVault().queryServiceRequestById(req.id, Vault.StateStatus.UNCONSUMED, proxy)
            ?: throw Exception("Service request ${req.id} not found")
        val status = ValidateFruitFlowStages().validateStages(result.state.data, req.fruitFlow)
        return try {
            ResponseEntity.status(HttpStatus.OK).body(status.toString())
        } catch (ex: Throwable) {
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(
        value = ["send-user-creation-email"],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun sendUserCreationEmail(
        @RequestBody request: UserCreationEmailDTO,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<String> {
        return try {
            val toAddress = request.emailId
            val to = mutableSetOf<String>(toAddress)
            val emalObj = SendEmail()
            val htmlFormatter = HtmlFormatter()
            val emailTemplate = htmlFormatter.newUserEmailTemplate(request)

            emalObj.sendEmail(
                EmailDataModel(
                    from = ConfigConstants.configData.supportEmail,
                    fromName = ConfigConstants.configData.supportEmailFrom,
                    to = to,
                    subject = "Agriledger Credentials",
                    emailTemplate = emailTemplate
                )
            )

            return ResponseEntity.status(HttpStatus.OK).body("Email Sent")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @GetMapping(value = ["get-flagged-transactions"], produces = [APPLICATION_JSON_VALUE])
    fun getFlaggedTransactions(
        pageNo: Int,
        pageSize: Int,
        @ApiIgnore authentication: Authentication
    ): ResponseEntity<MutableSet<FlaggedTransactionModel>> {
        val _pageSize = if (pageSize > 200) {
            DEFAULT_PAGE_SIZE
        } else {
            pageSize
        }
        val flaggedTransactions = mutableSetOf<FlaggedTransactionModel>()
        try {
            val queryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
            val results = builder {
                val index = PersistentSalesState::flaggedTransaction.equal(true)
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
                val finalCriteria = queryCriteria.and(customCriteria)
                proxy.vaultQueryBy<SalesState>(finalCriteria, PageSpecification(pageNo, _pageSize))
            }
            val response = results.states
            response.forEach {
                val vaultData = it.state.data
                var receivingPerson: String = ""
                var serviceRendered: String = ""
                var maxAmount: Float = 0f
                var identificationNo: String? = null
                var location: String? = null

                if (vaultData.paymentDistribution != null) {
                    vaultData.paymentDistribution?.lots?.forEach { lot ->
                        // producer data
                        if (lot.producerAmount > maxAmount) {
                            maxAmount = lot.producerAmount
                            receivingPerson = lot.producerName
                            serviceRendered = "Fruit Supply"
                            identificationNo = lot.identificationNo
                            location = lot.location
                        }
                    }
                    vaultData.paymentDistribution?.sales?.forEach { sale ->
                        if (sale.amount > maxAmount) {
                            maxAmount = sale.amount
                            receivingPerson = sale.role.toString()
                            serviceRendered = "Fruit Processing"
                            identificationNo = null
                            location = null
                        }
                    }
                    val data = FlaggedTransactionModel(
                        saleId = vaultData.saleId,
                        batchId = vaultData.batchId,
                        amount = maxAmount,
                        confirmPaymentDate = if (vaultData.confirmPaymentData != null) {
                            vaultData.confirmPaymentData?.confirmPaymentDate
                        } else {
                            null
                        },
                        location = location,
                        identificationNo = identificationNo,
                        receivingPerson = receivingPerson,
                        serviceRendered = serviceRendered
                    )
                    flaggedTransactions.add(data)
                }
            }
            return ResponseEntity.ok(flaggedTransactions)
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
        return ResponseEntity.ok(flaggedTransactions)
    }

    @PostMapping(value = ["trigger-email"], produces = [TEXT_PLAIN_VALUE])
    fun triggerEmail(@RequestBody request: EmailDataDTO, authentication: Authentication): ResponseEntity<String> {
        val emailObj = SendEmail()
        val htmlFormatter = HtmlFormatter()
        val to = request.to
        val ccAddresses = mutableSetOf<String>()
        if (request.cc != null) {
            request.cc?.forEach { ccAddresses.add(it) }
        }
        val emailTemplate = htmlFormatter.formatEmail(request.emailBody)
        val attachments = mutableListOf<Attachments>()
        if (request.attachments != null) {
            request.attachments?.forEach {
                attachments.add(
                    emailObj.generateEmailAttachment(
                        encodedContent = it.base64EncodedString,
                        filename = it.filename,
                        type = it.type
                    )
                )
            }
        }
        try {
            emailObj.sendEmail(
                EmailDataModel(
                    from = ConfigConstants.configData.supportEmail,
                    fromName = ConfigConstants.configData.supportEmailFrom,
                    to = to,
                    cc = ccAddresses,
                    subject = request.subject,
                    emailTemplate = emailTemplate,
                    attachments = attachments
                )
            )
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
        return ResponseEntity.status(HttpStatus.OK).body("Email Sent")
    }

    @DeleteMapping(
            value = ["delete-service-request"],
            produces = [TEXT_PLAIN_VALUE]
    )
    fun deleteServiceRequest(id: String, @ApiIgnore authentication: Authentication): ResponseEntity<String> {
        val userPrincipal: User = authentication.principal as User
        val email: String? = userPrincipal.email
        if (!ConfigConstants.configData.adminAddresses.contains(email)) {
            throw Exception("Only super user can access the data.")
        }
        return try {
            val signedTx = proxy.startTrackedFlow(::DeleteServiceRequest, id).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.OK).body("Transaction ${signedTx.id} successful \n service request $id deleted.\n")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
}