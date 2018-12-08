package pt.um.lei.masb.agent.messaging.block

import jade.content.onto.BasicOntology
import jade.content.onto.BeanOntology
import jade.content.onto.BeanOntologyException
import jade.content.onto.Ontology
import mu.KLogging

class BlockOntology @Throws(BeanOntologyException::class)
constructor() : BeanOntology(ONTOLOGY_NAME, BasicOntology.getInstance()) {

    init {
        add("pt.um.lei.masb.agent.messaging.transaction.ontology")
        add("pt.um.lei.masb.agent.messaging.block.ontology")
    }

    companion object : KLogging() {
        private val ONTOLOGY_NAME = "JBlock-Ontology"

        // The singleton instance of this ontology
        // This is the method to access the singleton music shop ontology object
        var instance: Ontology? = null
            private set

        init {
            try {
                instance = BlockOntology()
            } catch (e: BeanOntologyException) {
                logger.error("", e)
            }

        }
    }
}
