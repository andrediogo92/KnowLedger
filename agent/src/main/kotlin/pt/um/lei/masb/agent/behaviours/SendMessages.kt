package pt.um.lei.masb.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.Behaviour
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import mu.KLogging
import pt.um.lei.masb.agent.data.AgentPeers
import pt.um.lei.masb.agent.data.convertToJadeTransaction
import pt.um.lei.masb.agent.messaging.transaction.TransactionOntology
import pt.um.lei.masb.agent.messaging.transaction.ontology.actions.DiffuseTransaction
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.utils.RingBuffer

class SendMessages(
    private val sc: SideChain,
    private val rb: RingBuffer<Transaction>,
    private val agentPeers: AgentPeers,
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
                                sc.blockChainId,
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
