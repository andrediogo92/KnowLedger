package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept

data class JMerkleTree(
    val hashes: List<String>,
    val levelIndex: List<Int>
) : Concept
