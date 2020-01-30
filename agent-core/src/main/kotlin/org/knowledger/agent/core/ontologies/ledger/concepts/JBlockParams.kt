package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JBlockParams(
    var blockMemSize: Int,
    var blockLength: Int
) : Concept