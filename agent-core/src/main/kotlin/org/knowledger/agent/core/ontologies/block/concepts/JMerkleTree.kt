package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept

data class JMerkleTree(
    var hashes: List<String>,
    var levelIndex: List<Int>
) : Concept
