package org.knowledger.agent.core.ontologies

import jade.content.onto.BeanOntology

object TransactionOntology : BeanOntology(
    "JTransactionOntology", BlockOntology
) {

    const val ONTOLOGY_NAME = "JTransactionOntology"

    init {
        //Ontology made up of all the transaction classes.
        add("org.knowledger.agent.core.ontologies.transaction")
    }
}
