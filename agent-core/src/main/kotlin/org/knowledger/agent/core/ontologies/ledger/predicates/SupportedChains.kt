package org.knowledger.agent.core.ontologies.ledger.predicates

import jade.content.Predicate
import org.knowledger.base64.Base64String

data class SupportedChains(
    val types: List<Base64String>
) : Predicate