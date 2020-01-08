package org.knowledger.agent.core.ontologies

import jade.content.onto.BeanOntology

object LedgerOntology : BeanOntology(
    "JLedgerOntology", BlockOntology
) {
    const val ONTOLOGY_NAME = "JLedgerOntology"

    init {
        add("org.knowledger.agent.core.ontologies.ledger")
    }

}