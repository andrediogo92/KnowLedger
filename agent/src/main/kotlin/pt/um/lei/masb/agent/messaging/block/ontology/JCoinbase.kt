package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept

data class JCoinbase(
    val blockChainId: JBlockChainId?,
    val payoutTXO: Set<JTransactionOutput>,
    val coinbase: String,
    val hashId: String
) : Concept