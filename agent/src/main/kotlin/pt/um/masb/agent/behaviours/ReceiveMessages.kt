package pt.um.masb.agent.behaviours

import jade.content.lang.sl.SLCodec
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import org.tinylog.kotlin.Logger
import pt.um.masb.agent.data.convertToJadeBlock
import pt.um.masb.agent.messaging.block.BlockOntology
import pt.um.masb.agent.messaging.block.ontology.actions.DiffuseBlock
import pt.um.masb.agent.messaging.transaction.TransactionOntology
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.Transaction

/**
 * Behaviour for handling incoming messages related to Transactions and Blocks.
 * TODO: Split into behaviours for each.
 */
class ReceiveMessages(
    private val sc: ChainHandle,
    private val agentPeers: pt.um.masb.agent.data.AgentPeers,
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
            var missingNum = sc.lastBlockHeader.let {
                when (it) {
                    is LoadFailure.Success -> it.data.blockheight - rHeight
                    else -> -1
                }
            }
            sendMissingNum.content = (if (missingNum < 0) -1 else missingNum).toString()
            agent.send(sendMissingNum)

            //Send missing blocks
            loop@ while (missingNum > 0) {
                val codec = SLCodec()
                val blmsg = ACLMessage(ACLMessage.INFORM)
                val blk = sc.getBlockByHeight(rHeight)
                when (blk) {
                    is LoadFailure.Success -> {
                        myAgent.contentManager
                            .fillContent(
                                blmsg,
                                DiffuseBlock(
                                    convertToJadeBlock(blk.data, clazz),
                                    blocksReq.sender
                                )
                            )
                        blmsg.addReceiver(blocksReq.sender)
                        blmsg.language = codec.name
                        blmsg.ontology = BlockOntology.name
                        rHeight++
                        missingNum--
                    }
                    else -> {
                        Logger.error {
                            "Nonexistent block referenced at height: $rHeight"
                        }
                        break@loop
                    }
                }
            }
        }
    }

    override fun done(): Boolean {
        return false
    }
}
