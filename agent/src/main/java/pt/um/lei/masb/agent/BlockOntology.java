package pt.um.lei.masb.agent;

import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import pt.um.lei.masb.blockchain.Transaction;

public class BlockOntology extends BeanOntology {
    public static final String ONTOLOGY_NAME="Block-ontology";

    // The singleton instance of this ontology
    private static Ontology theInstance;

    static {
        try {
            theInstance = new BlockOntology();
        } catch (BeanOntologyException e) {
            e.printStackTrace();
        }
    }

    // This is the method to access the singleton music shop ontology object
    public static Ontology getInstance() {
        return theInstance;
    }

    public BlockOntology() throws BeanOntologyException {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        add(Transaction.class);
    }
}
