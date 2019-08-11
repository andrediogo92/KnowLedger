package org.knowledger.agent.messaging.ledger.ontology.concepts

import jade.content.Concept

data class JBlockParams(
    var blockMemSize: Long,
    var blockLength: Long
) : Concept