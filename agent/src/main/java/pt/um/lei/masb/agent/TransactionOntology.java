package pt.um.lei.masb.agent;

import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import pt.um.lei.masb.blockchain.Transaction;

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
        add(Transaction.class);
    }
}
