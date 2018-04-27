package pt.um.lei.masb.agent;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.TransactionInput;
import pt.um.lei.masb.blockchain.data.NoiseData;
import pt.um.lei.masb.blockchain.data.SensorData;

import javax.sound.sampled.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Queue;

public class DataCapturing extends Behaviour {

    private BlockChain bc;
    private Queue<Block> blockQueue;
    private PublicKey pk;
    private ArrayList<Transaction> tq;

    public DataCapturing(BlockChain bc, PublicKey pk, Queue<Block> blockQueue,ArrayList<Transaction> tq) {
        this.bc = bc;
        this.pk = pk;
        this.tq = tq;
        this.blockQueue = blockQueue;
    }

    @Override
    public void action() {
        var bl = bc.newBlock();

        var noise = new NoiseData(SoundCapturing());
        System.out.println("noiseLevel " + noise.getNoiseLevel());

        var sd = new SensorData(noise);
        var t = new Transaction(pk, sd);
        bl.addTransaction(t);
        tq.add(t);
        blockQueue.add(bl);
    }


    @Override
    public boolean done() {
        return true;
    }


    public int SoundCapturing() {
        short max=-1;
        int bytesRead;
        TargetDataLine line;
        //WAV format
        var format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                     44100,
                                     16,
                                     2,
                                     4,
                                     44100,
                                     false);
        var info = new DataLine.Info(TargetDataLine.class,
                                     format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            byte[] buffer = new byte[2000];
            bytesRead = line.read(buffer, 0, buffer.length);


            if (bytesRead >= 0) {
                max = (short) (buffer[0] + (buffer[1] << 8));
                for (int p = 2; p < bytesRead - 1; p += 2) {
                    var thisValue = (short) (buffer[p] + (buffer[p + 1] << 8));
                    if (thisValue > max) max = thisValue;
                }
            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
        return (int)max;
    }
}