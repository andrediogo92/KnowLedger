package org.knowledger.agent.messaging.block

import jade.content.onto.BasicOntology
import jade.content.onto.BeanOntology

object BlockOntology : BeanOntology(
    "JBlockOntology", BasicOntology.getInstance()
) {

    const val ONTOLOGY_NAME = "JBlockOntology"

    init {
        add("pt.um.masb.agent.messaging.block.ontology")
    }
}
