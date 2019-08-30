package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JTransaction
import java.util.*

data class JBlock(
    var data: SortedSet<JTransaction>,
    var coinbase: JCoinbase,
    var header: JBlockHeader,
    val merkleTree: JMerkleTree
) : Concept
