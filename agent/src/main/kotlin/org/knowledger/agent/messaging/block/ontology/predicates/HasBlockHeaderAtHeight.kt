package org.knowledger.agent.messaging.block.ontology.predicates

import jade.content.Predicate

data class HasBlockHeaderAtHeight(
    var blockheight: Long
) : Predicate