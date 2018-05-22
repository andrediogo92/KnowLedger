package pt.um.lei.masb.agent;

import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.agent.data.SoundCapturing;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.NoiseData;
import pt.um.lei.masb.blockchain.data.SensorData;

import javax.sound.sampled.LineUnavailableException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Queue;

public class DataCapturing extends Behaviour {

    private BlockChain bc;
    private Queue<Block> blockQueue;
    private Ident id;
    private ArrayList<Transaction> tq;

    public DataCapturing(BlockChain bc, Ident id, Queue<Block> blockQueue, ArrayList<Transaction> tq) {
        this.bc = bc;
        this.id = id;
        this.tq = tq;
        this.blockQueue = blockQueue;
    }

    @Override
    public void action() {
        var bl = bc.newBlock();

        SoundCapturing sc = null;
        try {
            sc = new SoundCapturing();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }


        if (sc != null) {
            var noise = new NoiseData(sc.getRms(),
                                      sc.getPeak(),
                                      new BigDecimal(41.5449583),
                                      new BigDecimal(-8.4257831));
            System.out.println("noiseLevel " + noise.getNoiseLevel());

            var sd = new SensorData(noise);
            var t = new Transaction(id, sd);
            bl.addTransaction(t);
            tq.add(t);
            blockQueue.add(bl);
        }
    }


    @Override
    public boolean done() {
        return true;
    }


}