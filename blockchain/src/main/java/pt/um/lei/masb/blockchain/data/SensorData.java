package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Sensor data must be categorized in order to allow serialization and de-serialization to and from JSON.
 * It can't be any arbitrary object, must be serializable.
 * <p>
 * Supported categories are those in {@link Category}.
 * This class is built through explicit composition.
 */
@Entity
public class SensorData implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    private final Category category;

    @Embedded
    private final NoiseData nd;

    @Embedded
    private final TemperatureData td;

    @Embedded
    private final HumidityData hd;

    @Embedded
    private final LuminosityData ld;

    @Embedded
    private final OtherData<? extends Serializable> od;

    protected SensorData() {
        category = null;
        nd = null;
        td = null;
        hd = null;
        ld = null;
        od = null;
    }

    public SensorData(NoiseData nd) {
        category = Category.NOISE;
        this.nd = nd;
        td = null;
        hd = null;
        ld = null;
        od = null;
    }

    public SensorData(TemperatureData td) {
        category = Category.TEMPERATURE;
        nd = null;
        this.td = td;
        hd = null;
        ld = null;
        od = null;
    }

    public SensorData(HumidityData hd) {
        category = Category.HUMIDITY;
        nd = null;
        td = null;
        this.hd = hd;
        ld = null;
        od = null;
    }

    public SensorData(LuminosityData ld) {
        category = Category.LUMINOSITY;
        nd = null;
        td = null;
        hd = null;
        this.ld = ld;
        od = null;
    }

    public SensorData(OtherData<? extends  Serializable> od) {
        category = Category.OTHER;
        nd = null;
        td = null;
        hd = null;
        ld = null;
        this.od = od;
    }

    public Category getCategory() {
        return category;
    }

    public NoiseData getNoiseData() {
        return nd;
    }

    public TemperatureData getTemperatureData() {
        return td;
    }

    public HumidityData getHumidityData() {
        return hd;
    }

    public LuminosityData getLuminosityData() {
        return ld;
    }

    public OtherData<? extends Serializable> getOtherData() { return od; }

    @Override
    public long getApproximateSize() {
        long classSize = ClassLayout.parseClass(this.getClass()).instanceSize();
        if (category == null) {
            switch (category) {
                case NOISE:
                    return nd.getApproximateSize() + classSize;
                case TEMPERATURE:
                    return td.getApproximateSize() + classSize;
                case HUMIDITY:
                    return hd.getApproximateSize() + classSize;
                case LUMINOSITY:
                    return ld.getApproximateSize() + classSize;
                case OTHER:
                    return od.getApproximateSize() + classSize;
                default:
                    return -1;
            }
        } else {
            return classSize;
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
        SensorData that = (SensorData) o;
        return category == that.category &&
                Objects.equals(nd, that.nd) &&
                Objects.equals(td, that.td) &&
                Objects.equals(hd, that.hd) &&
                Objects.equals(ld, that.ld) &&
                Objects.equals(od, that.od);
    }

    @Override
    public int hashCode() {

        return Objects.hash(category, nd, td, hd, ld, od);
    }

    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("SensorData {");
        if (category != null) {
            switch (category) {
                case NOISE:
                    sb.append(nd.toString());
                    break;
                case TEMPERATURE:
                    sb.append(td.toString());
                    break;
                case HUMIDITY:
                    sb.append(hd.toString());
                    break;
                case LUMINOSITY:
                    sb.append(ld.toString());
                    break;
                case OTHER:
                    sb.append(od.toString());
                    break;
            }
        }
        sb.append(" }");
        return sb.toString();
    }
}
