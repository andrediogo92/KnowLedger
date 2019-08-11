package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept
import org.knowledger.agent.messaging.transaction.ontology.concepts.JTransaction
import java.util.*

data class JBlock(
    var data: SortedSet<JTransaction>,
    var coinbase: JCoinbase,
    var header: JBlockHeader,
    val merkleTree: JMerkleTree
) : Concept
