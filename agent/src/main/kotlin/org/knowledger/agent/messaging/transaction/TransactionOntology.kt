package org.knowledger.agent.messaging.transaction

import jade.content.onto.BeanOntology
import org.knowledger.agent.messaging.block.BlockOntology

object TransactionOntology : BeanOntology(
    "JTransactionOntology", BlockOntology
) {

    const val ONTOLOGY_NAME = "JTransactionOntology"

    init {
        //Ontology made up of all the transaction classes.
        add("pt.um.masb.agent.messaging.transaction.ontology")
    }
}
