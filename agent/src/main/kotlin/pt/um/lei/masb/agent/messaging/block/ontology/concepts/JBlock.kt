package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept
import pt.um.lei.masb.agent.messaging.transaction.ontology.concepts.JTransaction

data class JBlock(
    var data: List<JTransaction>,
    var coinbase: JCoinbase,
    var header: JBlockHeader,
    val merkleTree: JMerkleTree,
    val clazz: String
) : Concept
