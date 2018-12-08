package pt.um.lei.masb.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.content.onto.BeanOntologyException
import jade.core.behaviours.Behaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy
import mu.KLogging
import pt.um.lei.masb.agent.messaging.transaction.TransactionOntology
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.utils.RingBuffer

class SendMessages<T : BlockChainData>(
    private val sc: SideChain,
    private val rb: RingBuffer<Transaction>,
    private val srl: SerializationStrategy<T>
) : Behaviour() {
    private val toSend: Transaction? = null


    @ImplicitReflectionSerializer
    override fun action() {
        val dfd = DFAgentDescription()
        try {
            val agentList = DFService.search(myAgent, dfd)
            val codec = SLCodec()
            var ontology: TransactionOntology? = null
            try {
                ontology = TransactionOntology()
            } catch (e: BeanOntologyException) {
                logger.error(e) {}
            }

            for (agent in agentList) {
                for (t in rb) {
                    val msg = ACLMessage(ACLMessage.INFORM)
                    //TODO: Make JTransaction predicates.
                    //myAgent.contentManager.fillContent(msg,
                    //                                   convertToJadeTransaction(blid, t, srl))
                    msg.addReceiver(agent.name)
                    msg.language = codec.name
                    msg.ontology = ontology!!.name
                }
            }
        } catch (e: FIPAException) {
            e.printStackTrace()
        }

    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
