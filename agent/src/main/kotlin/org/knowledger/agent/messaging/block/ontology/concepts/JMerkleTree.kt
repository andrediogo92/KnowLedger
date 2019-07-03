package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JMerkleTree(
    var hashes: List<String>,
    var levelIndex: List<Int>
) : Concept
