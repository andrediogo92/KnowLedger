package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JTransactionOutput(
    var pubkey: String,
    var prevCoinbase: String,
    var hashId: String,
    var payout: String,
    var tx: Set<String>
) : Concept
