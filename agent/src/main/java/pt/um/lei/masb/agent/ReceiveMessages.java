package pt.um.lei.masb.agent;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;

public class ReceiveMessages extends Behaviour {
    private Codec codec = new SLCodec();
    private Ontology txOntology,blOntology;
    private Transaction tx;
    private Block bl;
    private BlockChain bc;

    public ReceiveMessages (BlockChain bc)  {
        this.bc=bc;
        try {
            txOntology=new TransactionOntology();
            blOntology=new BlockOntology();
        } catch (BeanOntologyException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void action() {
        var mt = MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                     MessageTemplate.MatchOntology(txOntology.getName()) );


        var txmsg = myAgent.receive(mt);
        try {
            if (txmsg!=null) {
                ContentElement txce = myAgent.getContentManager().extractContent(txmsg);
                tx = (Transaction) txce;
                bc.getLastBlock().addTransaction(tx);
            }
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
        var blocksReq= myAgent.receive();
        if (blocksReq!=null){
            var rHeight=Long.parseLong(blocksReq.getContent());
            //Send number of missing blocks
            var sendMissingNum = new ACLMessage(ACLMessage.INFORM);
            int missingNum=bc.getLastBlock().getHeader().getBlockHeight()-rHeight;
            sendMissingNum.setContent(String.valueOf(missingNum<=0?0:missingNum));

            //Send missing blocks
            while (missingNum>0){
                var codec = new SLCodec();
                var blmsg = new ACLMessage(ACLMessage.INFORM);
                myAgent.getContentManager().fillContent(blmsg,bc.getBlockByHeight(rHeight));
                blmsg.addReceiver(blocksReq.getSender());
                blmsg.setLanguage(codec.getName());
                blmsg.setOntology(blOntology.getName());
                rHeight++;
                missingNum--;
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
