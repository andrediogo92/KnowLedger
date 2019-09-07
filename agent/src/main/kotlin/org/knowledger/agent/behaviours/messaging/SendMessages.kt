package org.knowledger.agent.behaviours.messaging

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.ParallelBehaviour
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import org.knowledger.agent.agents.BaseAgent
import org.knowledger.agent.core.ontologies.TransactionOntology
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseTransaction
import org.knowledger.agent.data.AgentPeers
import org.knowledger.agent.messaging.toJadeTransaction
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.tinylog.kotlin.Logger

data class SendMessages internal constructor(
    private val sc: ChainHandle,
    private val rb: MutableList<HashedTransaction>,
    private val agentPeers: AgentPeers,
    private val lAgent: BaseAgent
) : ParallelBehaviour(lAgent, WHEN_ALL) {
    init {
        addSubBehaviour(DiffuseTransactionBehaviour())
    }

    inner class DiffuseTransactionBehaviour : CyclicBehaviour(lAgent) {
        override fun action() {
            try {
                val codec = SLCodec()

                for (t in rb) {
                    val msg = ACLMessage(ACLMessage.PROPAGATE)
                    myAgent.contentManager.fillContent(
                        msg,
                        DiffuseTransaction(
                            t.toJadeTransaction()
                        )
                    )
                    msg.addReceiver(agentPeers.ledgerPeers)
                    msg.language = codec.name
                    msg.ontology = TransactionOntology.name
                }
            } catch (e: FIPAException) {
                Logger.error(e)
            }

        }
    }
}
