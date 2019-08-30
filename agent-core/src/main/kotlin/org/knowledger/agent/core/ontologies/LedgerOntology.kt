package org.knowledger.agent.core.ontologies

import jade.content.onto.BasicOntology
import jade.content.onto.BeanOntology

object LedgerOntology : BeanOntology(
    "JLedgerOntology", BasicOntology.getInstance()
) {
    const val ONTOLOGY_NAME = "JLedgerOntology"

    init {
        add("org.knowledger.agent.core.ontologies.ledger")
    }

}