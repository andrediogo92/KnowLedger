package pt.um.masb.agent

import jade.core.Agent
import jade.core.behaviours.ParallelBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import mu.KLogging
import pt.um.masb.agent.utils.unpackOrThrow
import pt.um.masb.agent.utils.unpackOrThrowAndDoOnNonExistent
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.LedgerService
import pt.um.masb.ledger.storage.Transaction

@Suppress("UNCHECKED_CAST")
class SingleChainAgent : Agent() {

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private var sessionRewards: Double = 0.0
    private val service = arguments[0] as LedgerService
    private val handle = arguments[1] as LedgerHandle
    private val toSend: MutableList<Transaction> = mutableListOf()
    private val agentPeers = pt.um.masb.agent.data.AgentPeers(this)

    override fun setup() {
        val dfd = DFAgentDescription()
        dfd.name = aid
        try {
            DFService.register(this, dfd)
        } catch (fe: FIPAException) {
            logger.error(fe) {}
        }

        val ident = service.getIdentById("agent0")

        sessionRewards = 0.0

        val cl = arguments[2] as Class<out BlockChainData>
        val storage = arguments[3] as AbstractStorageAdapter<out BlockChainData>
        val sc = handle.getChainHandleOf(cl).unpackOrThrowAndDoOnNonExistent {
            handle.registerNewChainHandleOf(cl, storage).unpackOrThrow()
        }

        val b = object : ParallelBehaviour(this, WHEN_ALL) {
            override fun onEnd(): Int {
                logger.info("Session Closed")
                return 0
            }
        }


//        b.addSubBehaviour(GetMissingBlocks(sc, agentPeers))
        b.addSubBehaviour(pt.um.masb.agent.behaviours.ReceiveMessages(sc, agentPeers, cl))
        ident?.apply {
            b.addSubBehaviour(pt.um.masb.agent.behaviours.CaptureData(sc, this, toSend))
        }
        b.addSubBehaviour(pt.um.masb.agent.behaviours.Mining(sc))
        b.addSubBehaviour(pt.um.masb.agent.behaviours.SendMessages(sc, toSend, agentPeers, cl))
        addBehaviour(b)
    }

    companion object : KLogging()

}
