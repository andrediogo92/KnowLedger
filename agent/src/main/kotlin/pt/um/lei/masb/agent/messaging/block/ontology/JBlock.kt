package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept
import pt.um.lei.masb.agent.messaging.transaction.ontology.JTransaction

data class JBlock(
    val data: List<JTransaction>,
    val coinbase: JCoinbase,
    val header: JBlockHeader,
    val merkleTree: JMerkleTree,
    val clazz: String
) : Concept
