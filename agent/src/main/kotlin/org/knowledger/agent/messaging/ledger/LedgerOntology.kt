package org.knowledger.agent.messaging.ledger

import jade.content.onto.BasicOntology
import jade.content.onto.BeanOntology

object LedgerOntology : BeanOntology(
    "JLedgerOntology", BasicOntology.getInstance()
) {
    const val ONTOLOGY_NAME = "JLedgerOntology"

    init {
        add("pt.um.masb.agent.messaging.ledger.ontology")
    }

}