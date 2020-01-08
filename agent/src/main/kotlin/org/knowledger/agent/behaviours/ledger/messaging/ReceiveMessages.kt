package org.knowledger.agent.behaviours.ledger.messaging

import jade.content.lang.Codec
import jade.content.onto.OntologyException
import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.ParallelBehaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import org.knowledger.agent.agents.AgentManager
import org.knowledger.agent.agents.ledger.ChainManager
import org.knowledger.agent.agents.ledger.PeerManager
import org.knowledger.agent.agents.ledger.TransactionManager
import org.knowledger.agent.core.ontologies.BlockOntology
import org.knowledger.agent.core.ontologies.TransactionOntology
import org.knowledger.agent.core.ontologies.block.actions.RequestBlocksFrom
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseTransaction
import org.knowledger.agent.messaging.and
import org.knowledger.agent.messaging.fromJadeHash
import org.knowledger.agent.messaging.fromJadeTransaction
import org.tinylog.kotlin.Logger

/**
 * Behaviour for handling incoming messages related to
 * Transactions, Blocks and Ledger notifications.
 */
class ReceiveMessages internal constructor(
    agent: Agent,
    private val agentManager: AgentManager,
    private val peerManager: PeerManager,
    private val chainManager: ChainManager,
    private val transactionManager: TransactionManager
) : ParallelBehaviour(agent, WHEN_ALL) {
    init {
        addSubBehaviour(HandleTransactionMessages())
        addSubBehaviour(HandleBlockMessages())
        addSubBehaviour(HandleLedgerMessages())
    }

    inner class HandleTransactionMessages : CyclicBehaviour(agent) {
        override fun action() {
            val mt =
                MessageTemplate.MatchOntology(TransactionOntology.name) and
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM)

            val txmsg = agent.receive(mt)
            if (txmsg != null) {
                try {
                    val txce = agent.contentManager.extractContent(txmsg)
                    Logger.info {
                        "Agent ${agent.aid}: Received transaction message -> $txce"
                    }
                    if (txce is DiffuseTransaction) {
                        Logger.info {
                            "Agent ${agent.aid}: Transaction is from ${txce.transaction.ledgerId?.hash}"
                        }
                        chainManager.chainBuilders.find {
                            it.tag == txce.transaction.ledgerId?.tag?.fromJadeHash()
                        }?.let { cb ->
                            chainManager.chainHandles.find {
                                it.chainHash == cb.chainHash
                            }?.addTransaction(
                                txce.transaction.fromJadeTransaction(cb)
                            ) ?: Logger.warn {
                                "No chain handles for ${txce.transaction.ledgerId?.tag}"
                            }
                        } ?: Logger.warn {
                            "No chain builder for ${txce.transaction.ledgerId?.tag}"
                        }
                    }
                } catch (e: Codec.CodecException) {
                    Logger.error(e)
                } catch (e: OntologyException) {
                    Logger.error(e)
                }
            } else {
                block()
            }
        }
    }

    inner class HandleBlockMessages : CyclicBehaviour(agent) {
        override fun action() {
            val mt = MessageTemplate.MatchOntology(BlockOntology.name) and
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)

            val blocksReq = agent.receive()
            if (blocksReq != null) {
                try {
                    val content =
                        agent.contentManager.extractContent(blocksReq)
                    //Send number of missing blocks
                    when (content) {
                        is RequestBlocksFrom -> {
                            respondToBlocks(content)
                        }
                    }
                } catch (e: Codec.CodecException) {
                    Logger.error(e)
                } catch (e: OntologyException) {
                    Logger.error(e)
                }
            }

        }

        private fun respondToBlocks(content: RequestBlocksFrom) {
        }

    }

    inner class HandleLedgerMessages : CyclicBehaviour(agent) {
        override fun action() {

        }

    }
}
