package pt.um.lei.masb.agent.behaviours

import jade.content.lang.Codec
import jade.content.lang.sl.SLCodec
import jade.content.onto.BeanOntologyException
import jade.content.onto.Ontology
import jade.content.onto.OntologyException
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy
import mu.KLogging
import pt.um.lei.masb.agent.messaging.block.BlockOntology
import pt.um.lei.masb.agent.messaging.transaction.TransactionOntology
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.BlockChainData

class ReceiveMessages<T : BlockChainData>(
    private val sc: SideChain,
    private val srl: SerializationStrategy<T>,
    private val clazz: Class<T>
) : Behaviour() {
    private val codec = SLCodec()
    private var txOntology: Ontology? = null
    private var blOntology: Ontology? = null
    private var tx: Transaction? = null
    private val bl: Block? = null

    init {
        try {
            txOntology = TransactionOntology()
            blOntology = BlockOntology()
        } catch (e: BeanOntologyException) {
            logger.error(e) {}
        }

    }

    @ImplicitReflectionSerializer
    @Suppress("UNCHECKED_CAST")
    override fun action() {
        val mt = MessageTemplate.and(
            MessageTemplate.MatchLanguage(codec.name),
            MessageTemplate.MatchOntology(txOntology!!.name)
        )


        val txmsg = myAgent.receive(mt)
        try {
            if (txmsg != null) {
                val txce = myAgent.contentManager.extractContent(txmsg)
                //TODO: Must extract a transaction from a content element.
                tx = txce as Transaction
                sc.lastBlock?.addTransaction(tx!!)
            }
        } catch (e: Codec.CodecException) {
            e.printStackTrace()
        } catch (e: OntologyException) {
            e.printStackTrace()
        }

        val blocksReq = myAgent.receive()
        if (blocksReq != null) {
            var rHeight = java.lang.Long.parseLong(blocksReq.content)
            //Send number of missing blocks
            val sendMissingNum = ACLMessage(ACLMessage.INFORM)
            var missingNum = sc.lastBlock?.header?.blockheight?.minus(rHeight) ?: 0
            sendMissingNum.content = (if (missingNum <= 0) 0 else missingNum).toString()

            //Send missing blocks
            while (missingNum > 0) {
                val codec = SLCodec()
                val blmsg = ACLMessage(ACLMessage.INFORM)
                val blk = sc.getBlockByHeight(rHeight)
                /*
                TODO: Make JBlock predicate
                myAgent.contentManager
                    .fillContent(blmsg,
                                 blk?.let {
                                       convertToJadeBlock(it, srl, clazz)
                                 })
                */
                blmsg.addReceiver(blocksReq.sender)
                blmsg.language = codec.name
                blmsg.ontology = blOntology!!.name
                rHeight++
                missingNum--
            }
        }
    }

    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()
}
