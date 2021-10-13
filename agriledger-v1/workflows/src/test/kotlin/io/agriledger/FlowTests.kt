package io.agriledger

//import io.agriledger.flows.Responder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.agriledger.flows.*
import io.agriledger.model.api.*
import io.agriledger.models.TestDataSet
import io.agriledger.utils.VaultDataUtility
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class FlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("io.agriledger.contracts"),
            TestCordapp.findCordapp("io.agriledger.flows")
    )))
    private val sae = network.createNode(MockNodeParameters(
            legalName = CordaX500Name(
                    organisation = "SAE",
                    locality = "Port-au-Prince",
                    country = "HT")
    ))
    private val lsp = network.createNode(MockNodeParameters(
            legalName = CordaX500Name(
                    organisation = "LSP",
                    locality = "Port-au-Prince",
                    country = "HT")
    ))
    private val collector = network.createNode(MockNodeParameters(
            legalName = CordaX500Name(
                    organisation = "Collector",
                    locality = "Port-au-Prince",
                    country = "HT")
    ))
    private val broker = network.createNode(MockNodeParameters(
            legalName = CordaX500Name(
                    organisation = "Broker",
                    locality = "New York",
                    country = "US")
    ))

    private val vaultDataUtility = VaultDataUtility()

    private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

    //private val str = Thread.currentThread().contextClassLoader.getResourceAsStream("test.yaml").bufferedReader().use { it.readText() }
    private val dataSet: TestDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("test_data.json"))
    //private val dataSet: TestDataSet = mapper.readValue(str)

    init {
        listOf(sae, lsp, collector, broker).forEach {
            it.registerInitiatedFlow(CreateServiceRequestFlow.Responder::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `CreateServiceRequest-Transaction Recording`() {
        val testName = "CreateServiceRequest-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }

        val flowInput: ServiceRequestCreationDTO = mapper.readValue(testData.flowInput.toString())

        val flow = CreateServiceRequestFlow.Initiator(flowInput, testData.email)

        val future = sae.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `AcceptServiceRequest-Transaction Recording`() {
        val testName = "AcceptServiceRequest-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }
        println(testData.toString())
        if (testData.serviceRequestVaultData != null) {
            vaultDataUtility.recordServiceRequestDataInVault(
                    sae, lsp, collector, broker, network, testData.serviceRequestVaultData
            )
        }

        val flowInput: AcceptServiceRequestDTO = mapper.readValue(testData.flowInput.toString())

        val flow = AcceptServiceRequestFlow.Initiator(flowInput, testData.email)
        val future = lsp.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `ArrivedAtPackhouse-Transaction Recording`() {
        val testName = "ArrivedAtPackhouse-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }

        if (testData.serviceRequestVaultData != null) {
            vaultDataUtility.recordServiceRequestDataInVault(
                    sae, lsp, collector, broker, network, testData.serviceRequestVaultData
            )
        }

        val flowInput: ArrivedAtPackHouseDTO = mapper.readValue(testData.flowInput.toString())

        val flow = CollectorArrivedAtPackHouseFlow.Initiator(flowInput, testData.email)
        val future = collector.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `FruitFlow-Transaction Recording`() {
        val testName = "FruitFlow-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }

        if (testData.serviceRequestVaultData != null) {
            vaultDataUtility.recordServiceRequestDataInVault(
                    sae, lsp, collector, broker, network, testData.serviceRequestVaultData
            )
        }

        val flowInput: FruitFlowDTO = mapper.readValue(testData.flowInput.toString())

        val flow = FruitFlow.Initiator(flowInput, testData.email)
        val future = lsp.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `CreateBatch-Transaction Recording`() {
        val testName = "CreateBatch-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }

        if (testData.serviceRequestVaultData != null) {
            vaultDataUtility.recordServiceRequestDataInVault(
                    sae, lsp, collector, broker, network, testData.serviceRequestVaultData
            )
        }

        val flowInput: BatchDTO = mapper.readValue(testData.flowInput.toString())

        val flow = CreateBatchFlow.Initiator(flowInput, testData.email)
        val future = lsp.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `NewSale-Transaction Recording`() {
        val testName = "NewSale-Transaction Recording"

        val testData = dataSet.tests.single { it.name == testName }

        if (testData.serviceRequestVaultData != null) {
            vaultDataUtility.recordServiceRequestDataInVault(
                    sae, lsp, collector, broker, network, testData.serviceRequestVaultData
            )
        }

        if (testData.batchData != null) {
            vaultDataUtility.recordBatchDataInVault(
                    sae, lsp, collector, broker, network, testData.batchData
            )
        }
        val flowInput: NewSaleDTO = mapper.readValue(testData.flowInput.toString())

        val flow = NewSaleFlow.Initiator(flowInput, testData.email)
        val future = broker.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()

        // We check the recorded transaction in all the vaults.
        for (node in listOf(sae, lsp, collector, broker)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }
}