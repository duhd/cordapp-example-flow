package com.example.external

import com.example.base.UserAccModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor
import org.eclipse.jetty.http.HttpStatus
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.ws.rs.core.MediaType


object ExternalAccountnameAPI {

    @CordaService
    class Service(val services: ServiceHub) : SingletonSerializeAsToken() {

        private companion object {
            val logger = loggerFor<Service>()
            val AccountNameURI = getAccountNameURI("AccountNameURI")
            val client = Client.create()
            val webResource = client.resource(AccountNameURI)
        }

        fun queryAccountName(value: UserAccModel): UserAccModel {
            try {

                val mapper = ObjectMapper()
                val response = webResource.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(ClientResponse::class.java, mapper.writeValueAsString(value))
                logger.info("Response from AccountNameURI " + response.status)
                if (response.status != HttpStatus.OK_200) {
                    throw RuntimeException("Failed : HTTP error code : "
                            + response.status)
                }
                val result = response.getEntity(String::class.java)
                return mapper.readValue<UserAccModel>(result)
            } catch (ex: Exception) {
                logger.error(ex.message)
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



