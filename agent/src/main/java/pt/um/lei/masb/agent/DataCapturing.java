package pt.um.lei.masb.agent;

import jade.core.behaviours.Behaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.agent.data.SoundCapturing;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.data.NoiseData;
import pt.um.lei.masb.blockchain.data.SensorData;
import pt.um.lei.masb.blockchain.utils.RingBuffer;

import javax.sound.sampled.LineUnavailableException;
import java.math.BigDecimal;
import java.util.Queue;

public class DataCapturing extends Behaviour {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCapturing.class);

    private BlockChain bc;
    private Queue<Block> blockQueue;
    private Ident id;
    private RingBuffer<Transaction> toSend;

    public DataCapturing(BlockChain bc,
                         Ident id,
                         Queue<Block> blockQueue,
                         RingBuffer<Transaction> tq) {
        this.bc = bc;
        this.id = id;
        this.toSend = tq;
        this.blockQueue = blockQueue;
    }

    @Override
    public void action() {
        var bl = bc.newBlock();

        SoundCapturing sc = null;
        try {
            sc = new SoundCapturing();
        } catch (LineUnavailableException e) {
            LOGGER.info("", e);
        }

        if (sc != null) {
            var noise =
                    new NoiseData(sc.getRms(),
                                  sc.getPeak(),
                                  new BigDecimal(41.5449583),
                                  new BigDecimal(-8.4257831));
            System.out.println("noiseLevel " + noise.getNoiseLevel());
            var sd = new SensorData(noise);
            var t = new Transaction(id, sd);
            toSend.offer(t);
            blockQueue.add(bl);
        }
    }


    @Override
    public boolean done() {
        return true;
    }


}