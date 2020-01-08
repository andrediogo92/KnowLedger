package org.knowledger.agent.behaviours.ledger.messaging

import jade.content.lang.sl.SLCodec
import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.ParallelBehaviour
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import org.knowledger.agent.agents.ledger.ChainManager
import org.knowledger.agent.agents.ledger.PeerManager
import org.knowledger.agent.agents.ledger.TransactionManager
import org.knowledger.agent.core.ontologies.TransactionOntology
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseTransaction
import org.knowledger.agent.messaging.toJadeTransaction
import org.tinylog.kotlin.Logger

internal class SendMessages constructor(
    agent: Agent,
    private val peerManager: PeerManager,
    private val chainManager: ChainManager,
    private val transactionManager: TransactionManager
) : ParallelBehaviour(agent, WHEN_ALL) {
    init {
        addSubBehaviour(DiffuseTransactionBehaviour())
    }

    internal inner class DiffuseTransactionBehaviour : CyclicBehaviour(agent) {
        override fun action() {
            try {
                val codec = SLCodec()

                val t = transactionManager.poll()
                val msg = ACLMessage(ACLMessage.PROPAGATE)
                msg.language = codec.name
                msg.ontology = TransactionOntology.name
                peerManager
                    .ledgerPeers
                    .peersByTagSupport(t.tag)
                    .forEach {
                        msg.addReceiver(it)
                    }
                myAgent.contentManager.fillContent(
                    msg,
                    DiffuseTransaction(
                        t.transaction.toJadeTransaction(
                            chainManager.findBuilder(t.tag)!!
                        )
                    )
                )
                agent.send(msg)
            } catch (e: FIPAException) {
                Logger.error(e)
            }

        }
    }
}
