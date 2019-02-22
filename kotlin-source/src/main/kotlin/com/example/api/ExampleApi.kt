package com.example.api

import com.example.base.UserAccModel
import com.example.flow.EnquireNameAccUserFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.node.services.IdentityService
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

// This API is accessible from /api/example. All paths specified below are relative to it.
@Path("example")
class ExampleApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<ExampleApi>()
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    @GET
    @Path("enquiry-acc")
    fun enquiryAcc(@QueryParam("accountNo") accountNo: String, @QueryParam("bic") bic: String): Response {
        if (accountNo == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'accountNo' is required.\n").build()
        }
        if (bic == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'bic' is is required.\n").build()
        }
        val otherParty = rpcOps.partiesFromName(bic, exactMatch = true)
        return Response.status(BAD_REQUEST).entity("Party named $bic cannot be found.\n").build()
        val accUser = UserAccModel(accountNo = accountNo, bic = bic)
        return try {
            val tx = rpcOps.startTrackedFlow(::EnquireNameAccUserFlow, accUser).returnValue.getOrThrow()
            Response.status(CREATED).entity("Result ${tx.toString()}\n").build()
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }
}