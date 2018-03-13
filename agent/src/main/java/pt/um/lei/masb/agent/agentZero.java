package pt.um.lei.masb.agent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Ident;

import java.security.PrivateKey;
import java.security.PublicKey;

public class agentZero extends Agent{

    //agent will try maximizing reward
    //sessionRewards could later be translated into user reward
    private double sessionRewards;
    private BlockChain bc;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Override
    protected void setup() {
        Ident i = new Ident();
        publicKey=i.getPublicKey();
        privateKey=i.getPrivateKey();
        sessionRewards=0;

        Object[] args = getArguments();
        this.bc=(BlockChain) args[0];

        ParallelBehaviour b= new ParallelBehaviour(this,ParallelBehaviour.WHEN_ANY) {
            @Override
            public int onEnd() {
                System.out.println("Session Closed");
                return 0;
            }
        };
        b.addSubBehaviour(new DataCapturing(bc,publicKey));
        b.addSubBehaviour(new Mining(bc));
        addBehaviour(b);
    }

}
