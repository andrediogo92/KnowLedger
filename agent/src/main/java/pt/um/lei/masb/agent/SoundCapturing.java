package pt.um.lei.masb.agent;

import jade.core.behaviours.OneShotBehaviour;
import pt.um.lei.masb.blockchain.data.NoiseData;

import javax.sound.sampled.*;

public class SoundCapturing extends OneShotBehaviour{

    private NoiseData noise;

    public SoundCapturing(NoiseData noise){
        this.noise=noise;
    }

    @Override
    public void action() {
        int bytesRead;
        TargetDataLine line;
        //WAV format
        AudioFormat format= new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,2,4,44100,false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) System.out.println("Line not supported");
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            AudioInputStream audioInputStream=new AudioInputStream(line);
            byte [] buffer = new byte[2000];
            bytesRead = line.read(buffer,0,buffer.length);

            short max;
            if (bytesRead >=0) {
                max = (short) (buffer[0] + (buffer[1] << 8));
                for (int p=2;p<bytesRead-1;p+=2) {
                    short thisValue = (short) (buffer[p] + (buffer[p+1] << 8));
                    if (thisValue>max) max=thisValue;
                }
            }
        } catch (LineUnavailableException ex) {
        }
    }
}
