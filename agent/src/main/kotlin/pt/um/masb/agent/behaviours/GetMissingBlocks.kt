package pt.um.masb.agent.behaviours

import jade.content.lang.Codec
import jade.content.lang.sl.SLCodec
import jade.content.onto.OntologyException
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import org.tinylog.kotlin.Logger
import pt.um.masb.agent.messaging.block.BlockOntology
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.results.LoadFailure
import kotlin.random.Random


class GetMissingBlocks(
    private val bc: ChainHandle,
    private val agentPeers: pt.um.masb.agent.data.AgentPeers
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
                msg.content = bc.lastBlockHeader.let {
                    when (it) {
                        is LoadFailure.Success -> it.data.blockheight.toString()
                        else -> null
                    }
                }

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
                                        //Convert JBlock to StorageUnawareBlock
                                        bc.addBlock(d.convertFromJadeBlock(bl))
                                    }
                                    */
                                } else {
                                    break
                                }
                            } catch (e: Codec.CodecException) {
                                Logger.error(e)
                            } catch (e: OntologyException) {
                                Logger.error(e)
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

}
