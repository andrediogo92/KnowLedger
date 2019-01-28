package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JCoinbase(
    var blockChainId: JBlockChainId?,
    var payoutTXO: Set<JTransactionOutput>,
    var coinbase: String,
    var hashId: String
) : Concept