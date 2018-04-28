package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.util.Objects;


/**
 * Ambient Noise Level measured in dB.
 */
@Embeddable
public class NoiseData implements Sizeable {
    private double noiseLevel;

    protected NoiseData() {}

    public NoiseData(double noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public double getNoiseLevel() {
        return this.noiseLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NoiseData noiseData = (NoiseData) o;
        return noiseLevel == noiseData.noiseLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(noiseLevel);
    }

    @Override
    public @NotNull String toString() {
        return "NoiseData { " +
                "noiseLevel=" + noiseLevel + ' ' +
                '}';
    }
}
