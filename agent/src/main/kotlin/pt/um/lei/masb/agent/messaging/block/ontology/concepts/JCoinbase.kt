package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JCoinbase(
    var ledgerId: JLedgerId?,
    var payoutTXO: Set<JTransactionOutput>,
    var coinbase: String,
    var hashId: String
) : Concept