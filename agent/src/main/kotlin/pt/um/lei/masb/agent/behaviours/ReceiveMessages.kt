package pt.um.lei.masb.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import mu.KLogging
import pt.um.lei.masb.agent.data.AgentPeers
import pt.um.lei.masb.agent.data.convertToJadeBlock
import pt.um.lei.masb.agent.messaging.block.BlockOntology
import pt.um.lei.masb.agent.messaging.block.ontology.actions.DiffuseBlock
import pt.um.lei.masb.agent.messaging.transaction.TransactionOntology
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.BlockChainData

/**
 * Behaviour for handling incoming messages related to Transactions and Blocks.
 * TODO: Split into behaviours for each.
 */
class ReceiveMessages(
    private val sc: SideChain,
    private val agentPeers: AgentPeers,
    private val clazz: Class<out BlockChainData>
//    private val srl: SerializationStrategy<T>
) : Behaviour() {
    private val codec = SLCodec()
    private lateinit var tx: Transaction
    private lateinit var bl: Block

    @Suppress("UNCHECKED_CAST")
    override fun action() {
        val mt = MessageTemplate.and(
            MessageTemplate.MatchLanguage(codec.name),
            MessageTemplate.MatchOntology(TransactionOntology.name)
        )


        /**
        val txmsg = myAgent.receive(mt)
        try {
        if (txmsg != null) {
        val txce = myAgent.contentManager.extractContent(txmsg)
        //TODO: Must extract a transaction from a content element.
        tx = txce as Transaction
        sc.lastBlock?.addTransaction(tx)
        }
        } catch (e: Codec.CodecException) {
        logger.error(e){}
        } catch (e: OntologyException) {
        logger.error(e){}
        }
         */

        val bt = MessageTemplate.and(
            MessageTemplate.and(
                MessageTemplate.MatchLanguage(codec.name),
                MessageTemplate.MatchOntology(BlockOntology.name)
            ),
            MessageTemplate.and(
                MessageTemplate.MatchProtocol("blockNotice"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            )
        )


        val blocksReq = myAgent.receive()
        if (blocksReq != null) {
            var rHeight = java.lang.Long.parseLong(blocksReq.content)
            //Send number of missing blocks
            val sendMissingNum = blocksReq.createReply()
            var missingNum = sc.lastBlock?.header?.blockheight?.minus(rHeight) ?: 0
            sendMissingNum.content = (if (missingNum <= 0) -1 else missingNum).toString()
            agent.send(sendMissingNum)

            //Send missing blocks
            while (missingNum > 0) {
                val codec = SLCodec()
                val blmsg = ACLMessage(ACLMessage.INFORM)
                val blk = sc.getBlockByHeight(rHeight)
                if (blk == null) {
                    logger.error {
                        "Inexistent block referenced at height: $rHeight"
                    }
                    break
                } else {
                    myAgent.contentManager
                        .fillContent(
                            blmsg,
                            DiffuseBlock(
                                convertToJadeBlock(blk, clazz),
                                blocksReq.sender
                            )
                        )
                    blmsg.addReceiver(blocksReq.sender)
                    blmsg.language = codec.name
                    blmsg.ontology = BlockOntology.name
                    rHeight++
                    missingNum--
                }
            }
        }
    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
