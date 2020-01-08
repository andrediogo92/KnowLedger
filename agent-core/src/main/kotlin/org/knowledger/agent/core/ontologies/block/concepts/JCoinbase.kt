package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash

data class JCoinbase(
    var payoutTXO: Set<JTransactionOutput>,
    var payout: String,
    var hashId: JHash,
    var formula: JHash?,
    var difficulty: JHash,
    var blockheight: Long
) : Concept