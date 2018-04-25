package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embeddable;

@Embeddable
public class NoiseData implements Sizeable {
    private int noiseLevel;

    public NoiseData() {
    }

    public int getNoiseLevel() {
        return this.noiseLevel;
    }

    public void setNoiseLevel(int nl) {
        noiseLevel = nl;
    }
}
