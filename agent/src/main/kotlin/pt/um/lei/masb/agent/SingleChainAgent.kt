package pt.um.lei.masb.agent

import jade.core.Agent
import jade.core.behaviours.ParallelBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import mu.KLogging
import pt.um.lei.masb.agent.behaviours.CaptureData
import pt.um.lei.masb.agent.behaviours.Mining
import pt.um.lei.masb.agent.behaviours.ReceiveMessages
import pt.um.lei.masb.agent.behaviours.SendMessages
import pt.um.lei.masb.agent.data.AgentPeers
import pt.um.lei.masb.agent.utils.unpackOrThrow
import pt.um.lei.masb.agent.utils.unpackOrThrowAndDoOnNonExistent
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.service.LedgerHandle
import pt.um.lei.masb.blockchain.service.LedgerService
import pt.um.lei.masb.blockchain.utils.RingBuffer

@Suppress("UNCHECKED_CAST")
class SingleChainAgent : Agent() {

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private var sessionRewards: Double = 0.0
    private val service = arguments[0] as LedgerService
    private val handle = arguments[1] as LedgerHandle
    private val toSend: RingBuffer<Transaction> = RingBuffer(3)
    private val agentPeers = AgentPeers(this)

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
        val sc = handle.getChainHandleOf(cl).unpackOrThrowAndDoOnNonExistent {
            handle.registerNewChainHandleOf(cl).unpackOrThrow()
        }

        val b = object : ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {
            override fun onEnd(): Int {
                logger.info("Session Closed")
                return 0
            }
        }


//        b.addSubBehaviour(GetMissingBlocks(sc, agentPeers))
        b.addSubBehaviour(ReceiveMessages(sc, agentPeers, cl))
        ident?.apply {
            b.addSubBehaviour(CaptureData(sc, this, toSend))
        }
        b.addSubBehaviour(Mining(sc))
        b.addSubBehaviour(SendMessages(sc, toSend, agentPeers, cl))
        addBehaviour(b)
    }

    companion object : KLogging()

}
