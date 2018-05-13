package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Luminosity data might be output by an ambient light sensor, using lux units
 * or a lighting unit, outputting a specific amount of lumens.
 */
@Entity
public final class LuminosityData extends GeoData implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private double lum;

    @Basic(optional = false)
    private LUnit unit;

    public LuminosityData(double lum,
                          LUnit unit,
                          BigDecimal lat,
                          BigDecimal lng) {
        super(lat, lng);
        this.lum = lum;
        this.unit = unit;
    }

    protected LuminosityData() {
        super(new BigDecimal(0), new BigDecimal(0));
    }

    /**
     * @return Luminosity reading, either from lighting units or light sensors.
     */
    public double getLum() {
        return lum;
    }

    /**
     * @return The unit of measurement, either lumen for lighting units or lux for light sensors.
     */
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
