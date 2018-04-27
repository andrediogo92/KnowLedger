package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.util.Objects;


/**
 * Humidity data can be expressed in Absolute/Volumetric or Relative humidity.
 * As such possible measurements can be in g/kg, Kg/kg or percentage.
 */
@Embeddable
public class HumidityData implements Sizeable {
    private double hum;
    private HUnit unit;

    public HumidityData(double hum,@NotNull HUnit unit) {
        this.hum = hum;
        this.unit = unit;
    }

    protected HumidityData() {
    }

    private HumidityData(HumidityData other) {
        this.hum = other.hum;
        this.unit = other.unit;
    }

    public @NotNull HumidityData clone(HumidityData hd) {
        return new HumidityData(hd);
    }

    public void convertToGbyKG() {
        switch(unit) {
            case G_BY_KG:
                break;
            case KG_BY_KG:
                hum *= 1000;
                unit = HUnit.G_BY_KG;
                break;
            case RELATIVE:
                break;
        }
    }

    public void convertToKGbyKG() {
        switch(unit) {
            case G_BY_KG:
                hum /= 1000;
                unit = HUnit.G_BY_KG;
                break;
            case KG_BY_KG:
                break;
            case RELATIVE:
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HumidityData that = (HumidityData) o;
        return Double.compare(that.hum, hum) == 0 &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(hum, unit);
    }

    @Override
    public @NotNull String toString() {
        return "HumidityData{" +
                "hum=" + hum +
                ", unit=" + unit +
                '}';
    }
}