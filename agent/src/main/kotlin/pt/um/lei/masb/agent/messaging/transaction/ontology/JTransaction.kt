package pt.um.lei.masb.agent.messaging.transaction.ontology


import jade.content.Concept
import pt.um.lei.masb.agent.messaging.block.ontology.JBlockChainId

/**
 * JTransaction in ontology bean form.
 */
data class JTransaction(
    val blockChainId: JBlockChainId?,
    val transactionId: String,
    val publicKey: String,
    val data: JPhysicalData,
    val signature: String
) : Concept {

    override fun toString(): String {
        return "JTransaction{" +
                "transactionId='" + transactionId + '\''.toString() +
                ", publicKey='" + publicKey + '\''.toString() +
                ", data=" + data +
                ", signature=" + signature +
                '}'.toString()
    }
}
