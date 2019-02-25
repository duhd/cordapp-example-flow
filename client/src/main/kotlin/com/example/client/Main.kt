package com.example.client

import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    Main().main(args)
}

private class Main {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Main::class.java)

        private val client = ClientRPC()
        private val USER_TAG = "corda"
        private val PASS_TAG = "not_blockchain"

    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: enquiry <node address>" }
        println(args[0])
        val parameters = args[0].split(" ")
        val nodeAddress = parameters[0]
        val bic = parameters[1]
        val accNo = parameters[2]

        setupTest(nodeAddress)

        val executor: ExecutorService = Executors.newFixedThreadPool(128)

        val forLoopMillisElapsed2 = measureTimeMillis {
            for (i in 0..9999) {
                val worker = Runnable {
                    runTest(bic, accNo)
                }
                executor.execute(worker)

            }

            executor.shutdown()

            while (!executor.isTerminated) {
            }
        }
        println("forLoopMillisElapsed: $forLoopMillisElapsed2")

        teardownTest()
    }

    private fun setupTest(nodeAddress: String) {
        val userRPC = USER_TAG
        val passRPC = PASS_TAG
        try {
            client.connect(nodeAddress, userRPC, passRPC)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error Connect RPC:", e)
        }
    }

    private fun runTest(bic: String, accNo: String) {
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