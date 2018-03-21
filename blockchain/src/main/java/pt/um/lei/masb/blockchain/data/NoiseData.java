package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

public class NoiseData implements Sizeable {
    private int noiseLevel;

    public NoiseData() {
    }

    public int getApproximateSize() {
        return 5;
    }

    public int getNoiseLevel() {
        return this.noiseLevel;
    }

    public void setNoiseLevel(int nl) {
        noiseLevel = nl;
    }
}
