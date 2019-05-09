package pt.um.masb.agent.messaging.transaction.ontology.concepts


import jade.content.Concept
import pt.um.masb.agent.messaging.block.ontology.concepts.JLedgerId

/**
 * JTransaction in ontology bean form.
 */
data class JTransaction(
    var ledgerId: JLedgerId? = null,
    var blockChainHash: String? = null,
    var transactionId: String,
    var publicKey: String,
    var data: JPhysicalData,
    var signature: String
) : Concept