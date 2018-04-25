package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Sensor data must be categorized in order to allow serialization and de-serialization to and from JSON.
 * It can't be any arbitrary object.
 * <p>
 * Supported categories for prototyping will be: noise, temperature, humidity and luminosity.
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
        switch (category) {
            case NOISE:
                return nd.getApproximateSize() + classSize;
            case HUMIDITY:
                return hd.getApproximateSize() + classSize;
            case TEMPERATURE:
                return td.getApproximateSize() + classSize;
            case LUMINOSITY:
                return ld.getApproximateSize() + classSize;
            case OTHER:
                return od.getApproximateSize() + classSize;
            default:
                return -1;
        }
    }
}
