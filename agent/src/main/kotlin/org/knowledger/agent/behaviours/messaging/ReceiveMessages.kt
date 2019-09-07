package org.knowledger.agent.behaviours.messaging

import jade.content.lang.Codec
import jade.content.onto.OntologyException
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.ParallelBehaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import org.knowledger.agent.agents.BaseAgent
import org.knowledger.agent.core.ontologies.BlockOntology
import org.knowledger.agent.core.ontologies.TransactionOntology
import org.knowledger.agent.core.ontologies.block.actions.RequestBlocksFrom
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseTransaction
import org.knowledger.agent.data.AgentPeers
import org.knowledger.agent.messaging.fromJadeTransaction
import org.knowledger.agent.misc.and
import org.knowledger.ledger.core.misc.base64DecodedToHash
import org.tinylog.kotlin.Logger

/**
 * Behaviour for handling incoming messages related to
 * Transactions, Blocks and Ledger notifications.
 */
data class ReceiveMessages internal constructor(
    private val agentPeers: AgentPeers,
    private val lAgent: BaseAgent
) : ParallelBehaviour(lAgent, WHEN_ALL) {
    init {
        addSubBehaviour(HandleTransactionMessages())
        addSubBehaviour(HandleBlockMessages())
        addSubBehaviour(HandleLedgerMessages())
    }

    inner class HandleTransactionMessages : CyclicBehaviour(lAgent) {
        override fun action() {
            val mt =
                MessageTemplate.MatchOntology(TransactionOntology.name) and
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM)

            val txmsg = lAgent.receive(mt)
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
                        lAgent.chainBuilders.find {
                            it.tag == txce.transaction.ledgerId?.tag?.base64DecodedToHash()
                        }?.let { cb ->
                            lAgent.chainHandles.find {
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

    inner class HandleBlockMessages : CyclicBehaviour(lAgent) {
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

    inner class HandleLedgerMessages : CyclicBehaviour(lAgent) {
        override fun action() {

        }

    }
}
