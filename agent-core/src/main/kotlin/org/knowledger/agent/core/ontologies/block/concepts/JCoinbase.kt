package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept

data class JCoinbase(
    var payoutTXO: Set<JTransactionOutput>,
    var payout: String,
    var hashId: String,
    var formula: String?,
    var difficulty: String,
    var blockheight: Long
) : Concept