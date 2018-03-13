package pt.um.lei.masb.agent;

import jade.core.behaviours.Behaviour;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;
import pt.um.lei.masb.blockchain.Transaction;
import pt.um.lei.masb.blockchain.TransactionInput;
import pt.um.lei.masb.blockchain.data.NoiseData;
import pt.um.lei.masb.blockchain.data.SensorData;

import javax.sound.sampled.*;
import java.security.PublicKey;
import java.util.ArrayList;

public class DataCapturing extends Behaviour {

    private BlockChain bc;
    private PublicKey pk;

    public DataCapturing(BlockChain bc, PublicKey pk) {
        this.bc = bc;
        this.pk = pk;
    }

    @Override
    public void action() {
        ArrayList<TransactionInput> l = new ArrayList<>();
        Block bl = new Block("someHash", 100);
        NoiseData noise = new NoiseData();
        noise.setNoiseLevel(SoundCapturing());

        SensorData sd = new SensorData(noise);
        Transaction t = new Transaction(pk, sd, l);

        bl.addTransaction(t);
        bc.addBlock(bl);
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
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) System.out.println("Line not supported");
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
                    short thisValue = (short) (buffer[p] + (buffer[p + 1] << 8));
                    if (thisValue > max) max = thisValue;
                }
            }
        } catch (LineUnavailableException ex) {
        }
        return (int)max;
    }
}