package pt.um.lei.masb.agent.data.transaction;

import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class TransactionOntology extends BeanOntology {
    public static final String ONTOLOGY_NAME="Transaction-ontology";

    // The singleton instance of this ontology
    private static Ontology theInstance;

    static {
        try {
            theInstance = new TransactionOntology();
        } catch (BeanOntologyException e) {
            e.printStackTrace();
        }
    }

    // This is the method to access the singleton music shop ontology object
    public static Ontology getInstance() {
        return theInstance;
    }

    public TransactionOntology() throws BeanOntologyException {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        //Ontology made up of all the transaction classes.
        add("pt.um.lei.masb.agent.transaction.ontology");
    }
}
