package pt.um.lei.masb.agent;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import pt.um.lei.masb.blockchain.Transaction;

import java.util.ArrayList;

public class SendMessages extends Behaviour {
    private ArrayList<Transaction> tq;

    public SendMessages(ArrayList<Transaction> tq){
        this.tq=tq;
    }


    @Override
    public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        try {
            DFAgentDescription[] agentList = DFService.search(myAgent, dfd);
            Codec codec = new SLCodec();
            TransactionOntology ontology= null;
            try {
                ontology = new TransactionOntology();
            } catch (BeanOntologyException e) {
                e.printStackTrace();
            }
            for (Transaction t : tq){
                for (DFAgentDescription agent : agentList){
                    ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
                    msg.addReceiver(agent.getName()); // sellerAID is the AID of the Seller agent
                    msg.setLanguage(codec.getName());
                    msg.setOntology(ontology.getName());
                }
                tq.remove(t);
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
