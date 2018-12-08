package pt.um.lei.masb.agent.messaging.transaction

import jade.content.onto.BasicOntology
import jade.content.onto.BeanOntology
import jade.content.onto.BeanOntologyException
import jade.content.onto.Ontology
import mu.KLogging

class TransactionOntology @Throws(BeanOntologyException::class)
constructor() : BeanOntology(ONTOLOGY_NAME, BasicOntology.getInstance()) {

    init {
        //Ontology made up of all the transaction classes.
        add("pt.um.lei.masb.agent.messaging.transaction.ontology")
    }

    companion object : KLogging() {
        val ONTOLOGY_NAME = "JTransaction-Ontology"

        // The singleton instance of this ontology
        // This is the method to access the singleton transaction ontology object
        var instance: Ontology? = null
            private set

        init {
            try {
                instance = TransactionOntology()
            } catch (e: BeanOntologyException) {
                logger.error("", e)
            }

        }
    }
}
