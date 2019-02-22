package com.example.client

import com.example.base.UserAccModel
import com.example.flow.EnquireNameAccUserFlow
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger

class ClientRPC {
    companion object {
        private var proxy: CordaRPCOps? = null
        private var conn: CordaRPCConnection? = null
        val logger: Logger = loggerFor<ClientRPC>()
    }

    fun connect(node: String, userRPC: String, passRPC: String) {
        val nodeAddress = NetworkHostAndPort.parse(node)
        val client = CordaRPCClient(nodeAddress)
        conn = client.start(userRPC, passRPC)
        proxy = conn!!.proxy
    }

    fun disconnect() {
        conn!!.notifyServerAndClose()
    }

    fun clientPay(bic: String, accountNo: String): String {
        val accUser = UserAccModel(accountNo = accountNo, bic = bic)
        return enquiryUserAccName(proxy!!, accUser)

    }

    fun enquiryUserAccName(proxy: CordaRPCOps, accUser: UserAccModel): String {
        val tx = proxy.startFlow(::EnquireNameAccUserFlow, accUser).returnValue.getOrThrow()
        return tx.toString()
    }
}
