package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash
import org.knowledger.base64.Base64String
import org.knowledger.collections.MutableSortedList

data class JWitness(
    var pubkey: Base64String,
    var previousWitnessIndex: Int,
    var prevCoinbase: JHash,
    var hash: JHash,
    var payout: String,
    var transactionOutputs: MutableSortedList<JTransactionOutput>
) : Concept, Comparable<JWitness> {
    override fun compareTo(other: JWitness): Int =
        pubkey.compareTo(other.pubkey)
}
