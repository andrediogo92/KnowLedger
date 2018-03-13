package pt.um.lei.masb.agent;

import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.TransactionInput;
import pt.um.lei.masb.blockchain.data.NoiseData;
import pt.um.lei.masb.blockchain.data.SensorData;

import java.security.PublicKey;
import java.util.ArrayList;

public class DataCapturing extends Behaviour {

    private BlockChain bc;
    private PublicKey pk;

    public DataCapturing(BlockChain bc, PublicKey pk){
        this.bc=bc;
        this.pk=pk;
    }
    @Override
    public void action() {
        ArrayList<TransactionInput> l=new ArrayList<>();
        Block bl=new Block("someHash",1);
        NoiseData noise=new NoiseData();
        myAgent.addBehaviour(new SoundCapturing(noise));

        SensorData sd=new SensorData(noise);
        Transaction t= new Transaction(pk,sd,l);

        bl.addTransaction(t);
        bc.addBlock(bl);
    }


    @Override
    public boolean done() {
        return true;
    }
}
