package pt.um.lei.masb.agent;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.um.lei.masb.blockchain.Transaction;

public class ReceiveMessages extends Behaviour {
    private Codec codec = new SLCodec();
    private Ontology ontology;
    private Transaction t;

    public ReceiveMessages (){

    }


    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.MatchOntology(ontology.getName()) );
        ACLMessage msg = myAgent.receive(mt);

        try {
            ContentElement ce= myAgent.getContentManager().extractContent(msg);
            t= (Transaction)ce;
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean done() {
        return false;
    }
}
