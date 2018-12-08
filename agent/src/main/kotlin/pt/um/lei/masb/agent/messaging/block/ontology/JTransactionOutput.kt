package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept

data class JTransactionOutput(
    val pubkey: String,
    val prevCoinbase: String,
    val hashId: String,
    val payout: String,
    val tx: Set<String>
) : Concept
