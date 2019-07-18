package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JLedgerId(
    var id: String,
    val hash: String
) : Concept
