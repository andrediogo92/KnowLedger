package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept

data class JTransactionOutput(
    var pubkey: String,
    var prevCoinbase: String,
    var hashId: String,
    var payout: String,
    var tx: Set<String>
) : Concept
