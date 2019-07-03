package org.knowledger.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.Behaviour
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import org.knowledger.agent.data.AgentPeers
import org.knowledger.agent.data.convertToJadeTransaction
import org.knowledger.agent.messaging.transaction.TransactionOntology
import org.knowledger.agent.messaging.transaction.ontology.actions.DiffuseTransaction
import org.knowledger.common.data.LedgerData
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.storage.Transaction
import org.tinylog.kotlin.Logger

class SendMessages(
    private val sc: ChainHandle,
    private val rb: MutableList<Transaction>,
    private val agentPeers: AgentPeers,
    private val clazz: Class<out LedgerData>
//    private val srl: SerializationStrategy<T>
) : Behaviour() {
    private val toSend: Transaction? = null


    override fun action() {
        try {
            val codec = SLCodec()

            for (agent in agentPeers.ledgerPeers) {
                for (t in rb) {
                    val msg = ACLMessage(ACLMessage.INFORM)
                    myAgent.contentManager.fillContent(
                        msg,
                        DiffuseTransaction(
                            convertToJadeTransaction(
                                sc.ledgerHash,
                                t
                            )
                        )
                    )
                    msg.addReceiver(agent)
                    msg.language = codec.name
                    msg.ontology = TransactionOntology.name
                }
            }
        } catch (e: FIPAException) {
            Logger.error(e)
        }

    }

    override fun done(): Boolean {
        return false
    }

}
