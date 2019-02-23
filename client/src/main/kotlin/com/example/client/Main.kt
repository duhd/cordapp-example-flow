package com.example.client

import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.ArtemisMessagingComponent
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    Main().main(args)
}

private class Main {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Main::class.java)

        private val client = ClientRPC()
        private val BIC_TAG = "BankB"
        private val ACCNO_TAG = "1111111"
        private val NODE_TAG = "51.77.128.44:10003"
        private val USER_TAG = "corda"
        private val PASS_TAG = "not_blockchain"

    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: enquiry <node address>" }
        val nodeAddress= args[0]
        setupTest(nodeAddress)
        runTest()
        teardownTest()
    }

    private fun setupTest(nodeAddress:String) {
        val userRPC = USER_TAG
        val passRPC = PASS_TAG
        try {
            client.connect(nodeAddress, userRPC, passRPC)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error Connect RPC:", e)
        }
    }

    private fun runTest() {
        val bic = BIC_TAG
        val accNo = ACCNO_TAG
        try {
            val responseMessage = client.clientEnquiry(bic, accNo)
            LOGGER.info("Response: $responseMessage")
        } catch (e: Exception) {
            LOGGER.error("Request was not successfully processed", e)
        }
    }

    private fun teardownTest() {
        client.disconnect()
    }
}