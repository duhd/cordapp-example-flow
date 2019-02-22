package com.example.flow

import com.example.base.UserAccModel
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class EnquiryAccFlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(listOf("com.example.flow"))
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b).forEach { it.registerInitiatedFlow(EnquireNameAccUserFlow::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `flow return Account Name null`() {
        val accUser = UserAccModel(accountNo = "11111111111", bic = b.info.singleIdentity().name.organisation)
        val flow = EnquireNameAccUserFlow(accUser)
        val future = a.startFlow(flow)
        network.runNetwork()
        val result = future.getOrThrow()
        assertEquals(result.accountName, "")
    }

    @Test
    fun `flow return Account Name ok`() {
        val accUser = UserAccModel(accountNo = "900100", bic = b.info.singleIdentity().name.organisation)
        val flow = EnquireNameAccUserFlow(accUser)
        val future = a.startFlow(flow)
        network.runNetwork()
        val result = future.getOrThrow()
        assertEquals(result.accountName, "")

    }

    @Test
    fun `flow return BIC not found`() {
        val accUser = UserAccModel(accountNo = "11111111111", bic = "ABC")
        val flow = EnquireNameAccUserFlow(accUser)
        val future = a.startFlow(flow)
        network.runNetwork()
        val result = future.getOrThrow()
        assertEquals(result.accountName, "")
    }
}