package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash
import org.knowledger.base64.Base64String

data class JLedgerId(
    var id: Base64String,
    val hash: JHash
) : Concept
