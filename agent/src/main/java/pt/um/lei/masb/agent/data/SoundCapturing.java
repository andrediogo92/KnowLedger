package pt.um.lei.masb.agent.data;

import javax.sound.sampled.*;

public class SoundCapturing {

    private double rms;
    private double peak;

    public SoundCapturing() throws LineUnavailableException {
        short max = -1;
        int bytesRead;
        TargetDataLine line;
        int bufSize = 2048;

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
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, bufSize);
        line.start();
        byte[] buf = new byte[bufSize];
        float[] samples = new float[bufSize / 2];
        for (int b; (b = line.read(buf, 0, buf.length)) > -1; ) {

            // convert bytes to samples here
            for (int i = 0, s = 0; i < b; ) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }

            for (float sample : samples) {

                float abs = Math.abs(sample);
                if (abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = Math.sqrt(rms / samples.length);
        }
    }

    /**
     * @return Root mean square of recorded values, [-1, 1] range.
     */
    public double getRms() {
        return rms;
    }

    /**
     * @return Peak of recorded values [0, 1].
     */
    public double getPeak() {
        return peak;
    }
}
