package org.knowledger.agent.core.ontologies.block.predicates

import jade.content.Predicate

data class HasBlockHeaderAtHeight(
    var blockheight: Long
) : Predicate