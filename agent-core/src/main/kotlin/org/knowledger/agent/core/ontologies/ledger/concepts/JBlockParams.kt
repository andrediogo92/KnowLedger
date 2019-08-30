package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JBlockParams(
    var blockMemSize: Long,
    var blockLength: Long
) : Concept