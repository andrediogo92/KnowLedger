package org.knowledger.agent.core.ontologies.ledger.predicates

import jade.content.Predicate
import org.knowledger.base64.Base64String

data class SearchLedger(
    val wanted: List<Base64String>
) : Predicate