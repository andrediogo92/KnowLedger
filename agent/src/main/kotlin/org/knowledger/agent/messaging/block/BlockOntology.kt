package org.knowledger.agent.messaging.block

import jade.content.onto.BeanOntology
import org.knowledger.agent.messaging.ledger.LedgerOntology

object BlockOntology : BeanOntology(
    "JBlockOntology", LedgerOntology
) {

    const val ONTOLOGY_NAME = "JBlockOntology"

    init {
        add("pt.um.masb.agent.messaging.block.ontology")
    }
}
