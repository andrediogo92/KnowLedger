package pt.um.lei.masb.agent;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.agent.data.DataConverter;
import pt.um.lei.masb.agent.messaging.block.BlockOntology;
import pt.um.lei.masb.agent.messaging.block.ontology.JBlock;
import pt.um.lei.masb.blockchain.BlockChain;

import java.util.Random;

public class getMissingBlocks extends Behaviour {
    private static final Logger LOGGER = LoggerFactory.getLogger(getMissingBlocks.class);
    private Codec codec = new SLCodec();
    private BlockChain bc;
    private Ontology blOntology;

    public getMissingBlocks(BlockChain bc){
        try {
            blOntology = new BlockOntology();
        } catch (BeanOntologyException e) {
            e.printStackTrace();
        }
        this.bc=bc;
    }

    @Override
    public void action() {
        Random random = new Random();
        int rnd=0,numR=0,numBl,previous=-1;
        boolean upToDate=false;

        DFAgentDescription dfd = new DFAgentDescription();
        DFAgentDescription[] agentList = new DFAgentDescription[0];

        DataConverter d = new DataConverter();
        try {
            agentList = DFService.search(myAgent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        while(!upToDate){
            do{
                rnd = random.nextInt(agentList.length);
            }while(rnd==previous);

            DFAgentDescription agent=agentList[rnd];
            var msg = new ACLMessage(ACLMessage.REQUEST);

            msg.addReceiver(agent.getName());
            msg.setContent(Long.toString(bc.getLastBlock().getHeader().getBlockheight()));

            //Receive number of missing blocks
            ACLMessage num= myAgent.blockingReceive(3000);

            if (num!=null) {
                numBl = Integer.parseInt(num.getContent());
                if (numBl!=0) {
                    //Receive blocks
                    var mb = MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                            MessageTemplate.MatchOntology(blOntology.getName()));
                    while(numR!=numBl) {
                        var blmsg = myAgent.blockingReceive(mb, 3000);
                        try {
                            if (blmsg != null) {
                                ContentElement blce = myAgent.getContentManager().extractContent(blmsg);
                                //Needs to be a message.
                                if (blce instanceof JBlock) {
                                    JBlock bl = (JBlock) blce;
                                    //Convert JBlock to Block
                                    bc.addBlock(d.convertFromJadeBlock(bl));
                                }
                            }else{
                                break;
                            }
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                        numR++;
                    }
                    if (numR==numBl) upToDate=true;
                }else{
                    upToDate=true;
                }
            }
            previous=rnd;
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
