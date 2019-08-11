package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JCoinbase(
    var payoutTXO: Set<JTransactionOutput>,
    var payout: String,
    var hashId: String,
    var formula: String?,
    var difficulty: String,
    var blockheight: Long
) : Concept