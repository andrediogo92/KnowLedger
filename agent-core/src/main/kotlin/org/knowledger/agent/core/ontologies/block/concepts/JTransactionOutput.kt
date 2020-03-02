package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash

data class JTransactionOutput(
    var payout: String,
    val prevTxBlock: JHash,
    var prevTxIndex: Int,
    var prevTx: JHash,
    var txIndex: Int,
    var tx: JHash
) : Concept, Comparable<JTransactionOutput> {
    override fun compareTo(other: JTransactionOutput): Int =
        txIndex.compareTo(other.txIndex)
}
