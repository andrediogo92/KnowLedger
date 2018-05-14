package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;


/**
 * Humidity data can be expressed in Absolute/Volumetric or Relative humidity.
 * As such possible measurements can be in g/kg, Kg/kg or percentage.
 */
@Entity
public final class HumidityData extends GeoData implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private double hum;

    @Basic(optional = false)
    private HUnit unit;

    public HumidityData(double hum,
                        @NotNull HUnit unit,
                        BigDecimal lat,
                        BigDecimal lng) {
        super(lat, lng);
        this.hum = hum;
        this.unit = unit;
    }

    protected HumidityData() {
        super(new BigDecimal(0), new BigDecimal(0));
    }

    public double convertToGbyKG() {
        var res = hum;
        switch(unit) {
            case G_BY_KG:
                break;
            case KG_BY_KG:
                res *= 1000;
                break;
            case RELATIVE:
                break;
        }
        return res;
    }

    public double convertToKGbyKG() {
        var res = hum;
        switch(unit) {
            case G_BY_KG:
                res /= 1000;
                break;
            case KG_BY_KG:
                break;
            case RELATIVE:
                break;
        }
        return res;
    }

    public double getHum() {
        return hum;
    }

    public HUnit getUnit() {
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