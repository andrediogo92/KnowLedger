package org.knowledger.agent.messaging.transaction.ontology.concepts


import jade.content.Concept
import org.knowledger.agent.messaging.ledger.ontology.concepts.JChainId

/**
 * JTransaction in ontology bean form.
 */
data class JTransaction(
    var ledgerId: JChainId,
    var transactionId: String,
    var publicKey: String,
    var data: JPhysicalData,
    var signature: String
) : Concept, Comparable<JTransaction> {
    override fun compareTo(other: JTransaction): Int =
        data.compareTo(other.data)

}