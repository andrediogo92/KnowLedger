package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Luminosity data might be output by an ambient light sensor, using lux units
 * or a lighting unit, outputting a specific amount of lumens.
 */
public class LuminosityData implements Sizeable {
    private int lum;
    private LUnit unit;

    public LuminosityData(int lum, LUnit unit) {
        this.lum = lum;
        this.unit = unit;
    }

    protected LuminosityData() {
    }

    public int getLum() {
        return lum;
    }

    public LUnit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LuminosityData that = (LuminosityData) o;
        return lum == that.lum &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(lum, unit);
    }

    @Override
    public String toString() {
        return "LuminosityData{" +
                "lum=" + lum +
                ", unit=" + unit +
                '}';
    }
}
