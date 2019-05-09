package pt.um.masb.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.Behaviour
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import mu.KLogging
import pt.um.masb.agent.data.convertToJadeTransaction
import pt.um.masb.agent.messaging.transaction.TransactionOntology
import pt.um.masb.agent.messaging.transaction.ontology.actions.DiffuseTransaction
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.ledger.Transaction
import pt.um.masb.ledger.service.ChainHandle

class SendMessages(
    private val sc: ChainHandle,
    private val rb: MutableList<Transaction>,
    private val agentPeers: pt.um.masb.agent.data.AgentPeers,
    private val clazz: Class<out BlockChainData>
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
            logger.error(e) { }
        }

    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
