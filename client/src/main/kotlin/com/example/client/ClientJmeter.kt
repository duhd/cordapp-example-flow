package com.example.client

import org.apache.jmeter.config.Arguments
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext
import org.apache.jmeter.samplers.SampleResult
import org.slf4j.LoggerFactory
import java.io.Serializable


class ClientJmeter : AbstractJavaSamplerClient(), Serializable {
    override fun getDefaultParameters(): Arguments {
        val defaultParameters = Arguments()
        defaultParameters.addArgument(NODE_TAG, "51.77.128.44:10003")
        defaultParameters.addArgument(USER_TAG, "corda")
        defaultParameters.addArgument(PASS_TAG, "not_blockchain")
        defaultParameters.addArgument(BIC_TAG, "BankB")
        defaultParameters.addArgument(ACCNO_TAG, "190200")
        defaultParameters.addArgument(METHOD_TAG, "enquireAcc")
        return defaultParameters
    }

    override fun setupTest(context: JavaSamplerContext) {
        val node = context.getParameter(NODE_TAG)
        val userRPC = context.getParameter(USER_TAG)
        val passRPC = context.getParameter(PASS_TAG)
        try {
            client.connect(node, userRPC, passRPC)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error Connect RPC:", e)
        }
    }

    override fun runTest(context: JavaSamplerContext): SampleResult {
        val method = context.getParameter(METHOD_TAG)
        val bic = context.getParameter(BIC_TAG)
        val accNo = context.getParameter(ACCNO_TAG)
        val sampleResult = SampleResult()

        try {
            sampleResult.sampleStart()
            if (method.equals("enquireAcc")) {
                sampleResult.responseMessage = client.clientEnquiry(bic, accNo)
            }
            sampleResult.sampleEnd()
            sampleResult.isSuccessful = java.lang.Boolean.TRUE
            sampleResult.setResponseCodeOK()
        } catch (e: Exception) {
            LOGGER.error("Request $method was not successfully processed", e)
            sampleResult.sampleEnd()
            sampleResult.responseMessage = e.message
            sampleResult.isSuccessful = java.lang.Boolean.FALSE
        }

        return sampleResult
    }

    override fun teardownTest(context: JavaSamplerContext) {
        client.disconnect()
    }

    companion object {
        private val client = ClientRPC()
        private val BIC_TAG = "bic"
        private val METHOD_TAG = "method"
        private val ACCNO_TAG = "accNo"
        private val NODE_TAG = "node"
        private val USER_TAG = "user"
        private val PASS_TAG = "pass"
        private val LOGGER = LoggerFactory.getLogger(ClientJmeter::class.java)
    }
}