package org.knowledger.agent.core.ontologies.transaction.concepts


import jade.content.Concept
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId
import org.knowledger.base64.Base64String

/**
 * JTransaction in JADE ontology bean form.
 */
data class JTransaction(
    var ledgerId: JChainId?,
    var transactionId: JHash,
    var publicKey: Base64String,
    var data: JPhysicalData,
    var signature: Base64String
) : Concept, Comparable<JTransaction> {
    override fun compareTo(other: JTransaction): Int =
        data.compareTo(other.data)

}