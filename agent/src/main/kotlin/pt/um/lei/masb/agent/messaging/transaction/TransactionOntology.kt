package pt.um.lei.masb.agent.messaging.transaction

import jade.content.onto.BeanOntology
import pt.um.lei.masb.agent.messaging.block.BlockOntology

object TransactionOntology : BeanOntology(
    "JTransaction-Ontology",
    BlockOntology
) {

    const val ONTOLOGY_NAME = "JTransaction-Ontology"

    init {
        //Ontology made up of all the transaction classes.
        add("pt.um.lei.masb.agent.messaging.transaction.ontology")
    }
}
