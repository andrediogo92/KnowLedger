package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash

data class JMerkleTree(
    var hashes: List<JHash>,
    var levelIndex: List<Int>
) : Concept
