package pt.um.lei.masb.agent.behaviours

import jade.content.lang.Codec
import jade.content.lang.sl.SLCodec
import jade.content.onto.OntologyException
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import mu.KLogging
import pt.um.lei.masb.agent.data.AgentPeers
import pt.um.lei.masb.agent.messaging.block.BlockOntology
import pt.um.lei.masb.blockchain.SideChain
import kotlin.random.Random


class GetMissingBlocks(
    private val bc: SideChain,
    private val agentPeers: AgentPeers
) : Behaviour() {
    private val codec = SLCodec()
    private val mustDo = false

    override fun action() {
        //Will later be shared variable.
        if (mustDo) {
            var rnd: Int
            var numR = 0
            var numBl: Int
            var upToDate = false

            while (!upToDate) {
                rnd = Random.nextInt(agentPeers.ledgerPeers.size)

                val agent = agentPeers.ledgerPeers[rnd]
                val msg = ACLMessage(ACLMessage.REQUEST)

                msg.addReceiver(agent)
                msg.content = bc.lastBlock?.header?.blockheight.toString()

                //Receive number of missing blocks
                val num = myAgent.blockingReceive(3000)

                if (num != null) {
                    numBl = num.content.toInt()
                    if (numBl != 0) {
                        //Receive blocks
                        val mb = MessageTemplate.and(
                            MessageTemplate.MatchLanguage(codec.name),
                            MessageTemplate.MatchOntology(BlockOntology.name)
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
    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
