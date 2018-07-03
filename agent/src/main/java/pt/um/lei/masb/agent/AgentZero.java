package pt.um.lei.masb.agent;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.utils.RingBuffer;

import java.util.ArrayDeque;
import java.util.Queue;

public class AgentZero extends Agent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentZero.class);

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private double sessionRewards;
    private BlockChain bc;
    private Queue<Block> bl;
    private RingBuffer<Transaction> toSend;

    @Override
    protected void setup() {
        DFAgentDescription dfd= new DFAgentDescription();
        dfd.setName(getAID());
        try {
            DFService.register( this, dfd );
        }
        catch (FIPAException fe) {
            LOGGER.error("", fe);
        }

        toSend= new RingBuffer<>(3);
        var i = new Ident();
        sessionRewards=0;

        Object[] args = getArguments();
        this.bc=(BlockChain) args[0];
        this.bl = new ArrayDeque<>(6);

        var b= new ParallelBehaviour(this,ParallelBehaviour.WHEN_ALL) {
            @Override
            public int onEnd() {
                LOGGER.info("Session Closed");
                return 0;
            }
        };
        b.addSubBehaviour(new getMissingBlocks(bc));
        b.addSubBehaviour(new ReceiveMessages(bc));
        b.addSubBehaviour(new DataCapturing(bc, i, bl, toSend));
        b.addSubBehaviour(new Mining(bc, bl));
        b.addSubBehaviour(new SendMessages(toSend));
        addBehaviour(b);
    }

}
