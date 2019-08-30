package org.knowledger.agent.core.ontologies.transaction.predicates

import jade.content.Predicate
import org.knowledger.agent.core.ontologies.transaction.concepts.JTransaction


data class DiffuseTransaction(
    var transaction: JTransaction
) : Predicate