package pt.um.lei.masb.agent.messaging.transaction.ontology.concepts


import jade.content.Concept
import pt.um.lei.masb.agent.messaging.block.ontology.concepts.JBlockChainId

/**
 * JTransaction in ontology bean form.
 */
data class JTransaction(
    var blockChainId: JBlockChainId? = null,
    var blockChainHash: String? = null,
    var transactionId: String,
    var publicKey: String,
    var data: JPhysicalData,
    var signature: String
) : Concept