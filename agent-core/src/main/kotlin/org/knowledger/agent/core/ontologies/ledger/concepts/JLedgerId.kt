package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JLedgerId(
    var id: String,
    val hash: String
) : Concept
