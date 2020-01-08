package org.knowledger.agent.behaviours.ledger

import jade.core.AID
import jade.core.Agent
import jade.core.behaviours.OneShotBehaviour
import jade.core.behaviours.SequentialBehaviour
import jade.core.behaviours.ThreadedBehaviourFactory
import jade.domain.DFService
import jade.lang.acl.ACLMessage
import org.knowledger.agent.agents.AgentManager
import org.knowledger.agent.agents.ledger.ChainManager
import org.knowledger.agent.agents.ledger.PeerManager
import org.knowledger.agent.agents.ledger.TransactionManager
import org.knowledger.agent.agents.searchLedger
import org.knowledger.agent.agents.searchSlave
import org.knowledger.agent.behaviours.ledger.InitializePeering.Bootstrap
import org.knowledger.agent.behaviours.ledger.InitializePeering.ContactSlaves
import org.knowledger.agent.behaviours.ledger.messaging.ReceiveMessages
import org.knowledger.agent.behaviours.ledger.messaging.SendMessages
import org.knowledger.agent.core.ontologies.ledger.predicates.SearchLedger
import org.knowledger.agent.core.ontologies.ledger.predicates.SupportedChains
import org.knowledger.agent.data.PeerBook
import org.knowledger.agent.messaging.state.ConversationIds
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.mapMutableSet
import org.knowledger.ledger.core.base.hash.hashFromHexString
import org.knowledger.ledger.data.LedgerData

/**
 * [InitializePeering] is composed of two behaviours:
 * 1. Cold [Bootstrap] to search for ledger agents sharing any subset of its supported types.
 * 2. [ContactSlaves] to search for its slave agents, which will supply [LedgerData].
 */
class InitializePeering internal constructor(
    agent: Agent,
    private val agentManager: AgentManager,
    private val peerManager: PeerManager,
    private val chainManager: ChainManager,
    private val transactionManager: TransactionManager,
    private val threadedBehaviourFactory: ThreadedBehaviourFactory
) : SequentialBehaviour(agent) {
    init {
        addSubBehaviour(Bootstrap())
        addSubBehaviour(ContactSlaves())
    }


    private fun registerPeers(
        peers: PeerBook,
        agents: Array<AID>,
        index: Int,
        chains: SupportedChains
    ) {
        peers.registerSet(
            chains.types.mapMutableSet {
                it.hashFromHexString()
            }, agents[index]
        )
    }

    private fun bootstrapBehaviour(
        agents: Array<AID>,
        conversationId: ConversationIds,
        peers: PeerBook
    ) {
        prepareMessage(
            agents, conversationId,
            ACLMessage.REQUEST,
            SearchLedger(
                chainManager.chainHandles.map {
                    it.id.tag.base64Encoded()
                }
            )
        )
        val replies: Array<SupportedChains?> =
            receiveReplies(agents, conversationId)

        registerReplies(replies) { i, t ->
            registerPeers(peers, agents, i, t)
        }

    }

    /**
     * [ContactSlaves] searches the [DFService] for agents which are marked for
     * slaving
     */
    private inner class ContactSlaves : OneShotBehaviour(agent) {
        override fun action() {
            val agents = agent.searchSlave()
            val conversationId = ConversationIds.SearchSlave
            bootstrapBehaviour(
                agents, conversationId,
                peerManager.slavePeers
            )
        }

    }

    private inner class Bootstrap : OneShotBehaviour(agent) {
        override fun action() {
            val agents = agent.searchLedger()
            val conversationId = ConversationIds.SearchLedger
            bootstrapBehaviour(
                agents, conversationId,
                peerManager.ledgerPeers
            )
        }
    }

    override fun onEnd(): Int {
        agentManager.trackBehaviour(ReceiveMessages(agent, agentManager, peerManager, chainManager, transactionManager))
        agentManager.trackBehaviour(SendMessages(agent, peerManager, chainManager, transactionManager))
        agentManager.trackThreaded(
            threadedBehaviourFactory.wrap(
                ManagePeers(agent, agentManager, peerManager, chainManager, transactionManager)
            )
        )
        return super.onEnd()
    }
}