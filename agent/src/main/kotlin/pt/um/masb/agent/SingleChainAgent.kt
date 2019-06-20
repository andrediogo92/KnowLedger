package pt.um.masb.agent

import jade.core.Agent
import jade.core.behaviours.ParallelBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import org.tinylog.kotlin.Logger
import pt.um.masb.agent.behaviours.CaptureData
import pt.um.masb.agent.behaviours.Mining
import pt.um.masb.agent.behaviours.ReceiveMessages
import pt.um.masb.agent.behaviours.SendMessages
import pt.um.masb.agent.data.AgentPeers
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.results.flatMapFailure
import pt.um.masb.common.results.onFailure
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.Transaction

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

        val ident: Identity = handle.getIdentById("agent0")!!

        sessionRewards = 0.0

        val cl = arguments[2] as Class<out BlockChainData>
        val storage = arguments[3] as AbstractStorageAdapter<out BlockChainData>
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
        cl: Class<out BlockChainData>,
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
