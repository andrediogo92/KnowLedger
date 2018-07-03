package pt.um.lei.masb.agent;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import pt.um.lei.masb.agent.messaging.transaction.TransactionOntology;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.utils.RingBuffer;

public class SendMessages extends Behaviour {
    private Transaction toSend;
    private RingBuffer<Transaction> rb;

    public SendMessages(RingBuffer<Transaction> tq){
        this.rb=tq;
    }


    @Override
    public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        try {
            DFAgentDescription[] agentList = DFService.search(myAgent, dfd);
            var codec = new SLCodec();
            TransactionOntology ontology= null;
            try {
                ontology = new TransactionOntology();
            } catch (BeanOntologyException e) {
                e.printStackTrace();
            }
            for (DFAgentDescription agent : agentList){
                for(Transaction t: rb) {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    //Transaction of the blockchain is not JADE serializable
                    //Need to convert into JTransaction of transaction ontology, in transaction.ontology package
                    //in order to actually send it.
                    myAgent.getContentManager().fillContent(msg,t);
                    msg.addReceiver(agent.getName());
                    msg.setLanguage(codec.getName());
                    msg.setOntology(ontology.getName());
                }
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
