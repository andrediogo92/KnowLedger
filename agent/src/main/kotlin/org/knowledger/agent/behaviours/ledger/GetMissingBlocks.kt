package org.knowledger.agent.behaviours.ledger

import jade.core.Agent
import jade.core.behaviours.OneShotBehaviour
import jade.lang.acl.ACLMessage
import org.knowledger.agent.core.ontologies.block.actions.RequestBlocksFrom
import org.knowledger.agent.data.PeerBook
import org.knowledger.ledger.service.handles.ChainHandle
import kotlin.random.Random


internal class GetMissingBlocks constructor(
    agent: Agent,
    private val bc: ChainHandle,
    private val agentPeers: PeerBook
) : OneShotBehaviour(agent) {

    override fun action() {
        val peers = agentPeers.peersByTagSupport(bc.id.tag)
        val rnd: Int = Random.nextInt(peers.size)
        val msg = ACLMessage(ACLMessage.REQUEST)

        msg.addReceiver(peers[rnd])
        agent.contentManager.fillContent(
            msg, agent.action(RequestBlocksFrom(bc.currentBlockheight))
        )
    }
}