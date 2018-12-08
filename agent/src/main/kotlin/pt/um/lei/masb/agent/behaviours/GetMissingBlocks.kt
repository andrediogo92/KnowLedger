package pt.um.lei.masb.agent.behaviours

import jade.content.lang.Codec
import jade.content.lang.sl.SLCodec
import jade.content.onto.BeanOntologyException
import jade.content.onto.Ontology
import jade.content.onto.OntologyException
import jade.core.behaviours.Behaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import mu.KLogging
import pt.um.lei.masb.agent.messaging.block.BlockOntology
import pt.um.lei.masb.blockchain.SideChain
import kotlin.random.Random


class GetMissingBlocks(
    private val bc: SideChain
) : Behaviour() {
    private val codec = SLCodec()
    private var blOntology: Ontology? = null

    init {
        try {
            blOntology = BlockOntology()
        } catch (e: BeanOntologyException) {
            logger.error(e) {}
        }

    }

    override fun action() {
        var rnd: Int
        var numR = 0
        var numBl: Int
        var upToDate = false

        val dfd = DFAgentDescription()
        var agentList = arrayOfNulls<DFAgentDescription>(0)

        try {
            agentList = DFService.search(myAgent, dfd)
        } catch (e: FIPAException) {
            logger.error("", e)
        }

        while (!upToDate) {
            rnd = Random.nextInt(agentList.size)

            val agent = agentList[rnd]
            val msg = ACLMessage(ACLMessage.REQUEST)

            msg.addReceiver(agent?.name)
            msg.content = bc.lastBlock?.header?.blockheight.toString()

            //Receive number of missing blocks
            val num = myAgent.blockingReceive(3000)

            if (num != null) {
                numBl = num.content.toInt()
                if (numBl != 0) {
                    //Receive blocks
                    val mb = MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.name),
                        MessageTemplate.MatchOntology(blOntology!!.name)
                    )
                    while (numR != numBl) {
                        val blmsg = myAgent.blockingReceive(mb, 3000)
                        try {
                            if (blmsg != null) {
                                val blce = myAgent.contentManager.extractContent(blmsg)
                                //Needs to be a message.
                                /*
                                TODO: Make JBlocks and JTransactions predicates
                                if (blce is JBlock) {
                                    val bl = blce as JBlock
                                    //Convert JBlock to Block
                                    bc.addBlock(d.convertFromJadeBlock(bl))
                                }
                                */
                            } else {
                                break
                            }
                        } catch (e: Codec.CodecException) {
                            logger.error(e) {}
                        } catch (e: OntologyException) {
                            logger.error(e) {}
                        }
                        numR++
                    }
                    if (numR == numBl) upToDate = true
                } else {
                    upToDate = true
                }
            }
        }
    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
