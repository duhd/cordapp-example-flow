package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.base.UserAccModel
import com.example.external.ExternalAPI
import com.example.external.ExternalAccountnameAPI
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class EnquireNameAccUserFlow(private val userAcc: UserAccModel) : FlowLogic<UserAccModel>() {
    companion object {
        object FIND_PARTY : Step("Generating transaction based on new IOU.")
        object SEND_REQUEST : Step("Verifying contract constraints.")
        object GATHERING_RESULT : Step("Signing transaction with our private key.")

        fun tracker() = ProgressTracker(
                FIND_PARTY,
                SEND_REQUEST,
                GATHERING_RESULT
        )
    }

    override val progressTracker = tracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    override fun call(): UserAccModel {
        progressTracker.currentStep = FIND_PARTY
        val bic = userAcc.bic.toString()
        val maybeOtherParty = serviceHub.identityService.partiesFromName(bic, exactMatch = true)
        when (maybeOtherParty.size != 1) {
            true -> return UserAccModel(accountNo = "", accountName = "", bic = "", X500Name = "")
            false -> {
                if (maybeOtherParty.first() == ourIdentity) throw IllegalArgumentException("Failed requirement: No enquiry by self")
                val otherParty = maybeOtherParty.single()
                progressTracker.currentStep = SEND_REQUEST
                val session = initiateFlow(otherParty)
                val resp = session.sendAndReceive<UserAccModel>(userAcc)
                progressTracker.currentStep = GATHERING_RESULT
                return resp.unwrap { it }
            }
        }
    }

    @InitiatedBy(EnquireNameAccUserFlow::class)
    class EnquireNameAccUserHandler(private val session: FlowSession) : FlowLogic<Unit>() {

        private companion object {

            val RECEIVING_REQUEST = object : ProgressTracker.Step("Receiving query account name request") {}
            val INVOKING_API_SERVICE = object : ProgressTracker.Step("Invoking api service for the account name") {}
            val RETURNING_ACC = object : ProgressTracker.Step("Return name of account") {}
        }

        override val progressTracker = ProgressTracker(RECEIVING_REQUEST, INVOKING_API_SERVICE, RETURNING_ACC)

        @Suspendable
        override fun call() {
            progressTracker.currentStep = RECEIVING_REQUEST
            val account: UserAccModel = session.receive<UserAccModel>().unwrap { it }

            progressTracker.currentStep = INVOKING_API_SERVICE
            val AccountNameAPI = serviceHub.cordaService(ExternalAPI.Service::class.java)
            val result = AccountNameAPI.queryAccountName(account)

            progressTracker.currentStep = RETURNING_ACC
            session.send(result)
        }
    }
}