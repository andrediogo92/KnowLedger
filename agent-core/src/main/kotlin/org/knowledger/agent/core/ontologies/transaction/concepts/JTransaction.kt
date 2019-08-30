package org.knowledger.agent.core.ontologies.transaction.concepts


import jade.content.Concept
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId

/**
 * JTransaction in JADE ontology bean form.
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