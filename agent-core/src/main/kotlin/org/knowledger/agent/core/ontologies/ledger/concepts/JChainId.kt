package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JChainId(
    var tag: String,
    val hash: String,
    var ledger: String
) : Concept