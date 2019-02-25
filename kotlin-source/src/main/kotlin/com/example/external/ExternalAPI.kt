package com.example.external

import com.example.base.UserAccModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import org.slf4j.LoggerFactory

object ExternalAPI {

    @CordaService
    class Service(val services: ServiceHub) : SingletonSerializeAsToken() {

        companion object {
            val LOGGER = LoggerFactory.getLogger(ExternalAPI::class.java)
            private val URL = getAccountNameURI("AccountNameURI")
            val JSON = MediaType.parse("application/json;charset=utf-8")
        }

        fun queryAccountName(value: UserAccModel): UserAccModel {
            try {
                val mapper = ObjectMapper()
                val body = RequestBody.create(JSON, mapper.writeValueAsString(value))
                val httpRequest = Request.Builder()
                        .url(URL)
                        .post(body)
                        .build()
                // BE CAREFUL when making HTTP calls in flows:
                // 1. The request must be executed in a BLOCKING way. Flows don't
                //    currently support suspending to await an HTTP call's response
                // 2. The request must be idempotent. If the flow fails and has to
                //    restart from a checkpoint, the request will also be replayed

                val httpResponse = OkHttpClient().newCall(httpRequest).execute()
                if (!httpResponse.isSuccessful) {
                    throw RuntimeException("Failed : HTTP error code : "
                            + httpResponse.code())
                }
                val result = httpResponse.body().string()
                return mapper.readValue<UserAccModel>(result)
            } catch (ex: Exception) {
                LOGGER.error(ex.message)
                return UserAccModel(accountNo = "", accountName = "", bic = "", X500Name = "")
            }
        }


    }

    // Try to read config properties to get the approve redeem URI
    fun getAccountNameURI(value: String): String {
        val prop = Properties()
        var input: InputStream? = null

        try {
            input = FileInputStream("./config.properties")
            //input = this.javaClass.getResource("/config.properties").openStream()
            // load a properties file
            prop.load(input)
            val result = prop.getProperty(value)
            return result
        } catch (ex: IOException) {
            throw ex
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            } else {
                throw IllegalArgumentException("config.properties not found or is null")
            }
        }
    }
}



