package org.knowledger.agent

import jade.core.Agent
import jade.core.behaviours.ParallelBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import org.knowledger.agent.behaviours.CaptureData
import org.knowledger.agent.behaviours.Mining
import org.knowledger.agent.behaviours.ReceiveMessages
import org.knowledger.agent.behaviours.SendMessages
import org.knowledger.agent.data.AgentPeers
import org.knowledger.common.data.LedgerData
import org.knowledger.common.results.flatMapFailure
import org.knowledger.common.results.onFailure
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.storage.Transaction
import org.tinylog.kotlin.Logger

@Suppress("UNCHECKED_CAST")
class SingleChainAgent : Agent() {

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private var sessionRewards: Double = 0.0
    private val handle = arguments[1] as LedgerHandle
    private val toSend: MutableList<Transaction> = mutableListOf()
    private val agentPeers = AgentPeers(this)

    override fun setup() {
        val dfd = DFAgentDescription()
        dfd.name = aid
        try {
            DFService.register(this, dfd)
        } catch (fe: FIPAException) {
            Logger.error(fe)
        }

        val ident: Identity = handle.getIdentityByTag("agent0")!!

        sessionRewards = 0.0

        val cl = arguments[2] as Class<out LedgerData>
        val storage = arguments[3] as AbstractStorageAdapter<out LedgerData>
        handle
            .getChainHandleOf(cl)
            .flatMapFailure {
                handle.registerNewChainHandleOf(cl, storage)
            }.onFailure {
                when (it.failure) {
                    is LedgerFailure.UnknownFailure ->
                        throw RuntimeException((it.failure as LedgerFailure.UnknownFailure).exception)
                }
                throw RuntimeException(it.failure.cause)
            }.also {
                registerBehaviours(ident, cl, it)
            }
    }

    private fun registerBehaviours(
        ident: Identity,
        cl: Class<out LedgerData>,
        chainHandle: ChainHandle
    ) {
        val b = object : ParallelBehaviour(this, WHEN_ALL) {
            override fun onEnd(): Int {
                Logger.info("Session Closed")
                return 0
            }
        }

        //b.addSubBehaviour(GetMissingBlocks(sc, agentPeers))
        b.addSubBehaviour(ReceiveMessages(chainHandle, agentPeers, cl))
        b.addSubBehaviour(CaptureData(chainHandle, ident, toSend))
        b.addSubBehaviour(Mining(chainHandle))
        b.addSubBehaviour(SendMessages(chainHandle, toSend, agentPeers, cl))
        addBehaviour(b)

    }
}
