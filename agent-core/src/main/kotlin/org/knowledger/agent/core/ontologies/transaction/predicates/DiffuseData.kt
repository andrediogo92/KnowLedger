package org.knowledger.agent.core.ontologies.transaction.predicates

import jade.content.Predicate
import org.knowledger.agent.core.ontologies.transaction.concepts.JPhysicalData

data class DiffuseData(val data: JPhysicalData) : Predicate