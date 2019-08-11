package org.knowledger.agent.messaging.ledger.ontology.concepts

import jade.content.Concept

data class JChainId(
    var tag: String,
    val hash: String,
    var ledger: String
) : Concept