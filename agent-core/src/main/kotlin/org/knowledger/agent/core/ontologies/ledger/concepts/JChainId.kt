package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash

data class JChainId(
    var tag: JHash,
    val hash: JHash,
    var ledger: JHash
) : Concept