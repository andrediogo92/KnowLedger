package pt.um.lei.masb.agent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class agentZero extends Agent{

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private double sessionRewards;
    private BlockChain bc;
    private Queue<Block> bl;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private ArrayList<Transaction> toSend;

    @Override
    protected void setup() {
        DFAgentDescription dfd= new DFAgentDescription();
        dfd.setName(getAID());
        try {
            DFService.register( this, dfd );
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        toSend= new ArrayList<>();
        var i = new Ident();
        publicKey=i.getPublicKey();
        privateKey=i.getPrivateKey();
        sessionRewards=0;

        Object[] args = getArguments();
        this.bc=(BlockChain) args[0];
        this.bl = new ArrayDeque<>(6);

        var b= new ParallelBehaviour(this,ParallelBehaviour.WHEN_ALL) {
            @Override
            public int onEnd() {
                System.out.println("Session Closed");
                return 0;
            }
        };
        b.addSubBehaviour(new DataCapturing(bc,publicKey, bl,toSend));
        b.addSubBehaviour(new Mining(bc, bl));
        b.addSubBehaviour(new ReceiveMessages());
        b.addSubBehaviour(new SendMessages(toSend));
        addBehaviour(b);
    }

}
