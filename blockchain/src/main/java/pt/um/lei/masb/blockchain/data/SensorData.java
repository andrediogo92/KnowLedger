package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.ClassLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * Sensor data must be categorized in order to allow serialization and de-serialization to and from JSON.
 * It can't be any arbitrary object, must be serializable.
 * <p>
 * Supported categories are those in {@link Category}.
 * This class is built through implicit composition.
 */
@Entity
public final class SensorData implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.ORDINAL)
    private final Category category;
    @OneToOne(optional = false)
    private final GeoData data;
    @Basic(optional = false)
    private final Instant t;

    protected SensorData() {
        category = null;
        data = null;
        t = null;
    }

    public SensorData(@NotNull NoiseData nd) {
        category = Category.NOISE;
        data = nd;
        t = Instant.now();
    }

    public SensorData(@NotNull NoiseData nd,
                      @NotNull Instant t) {
        category = Category.NOISE;
        data = nd;
        this.t = t;
    }


    public SensorData(@NotNull TemperatureData td) {
        category = Category.TEMPERATURE;
        data = td;
        t = Instant.now();
    }

    public SensorData(@NotNull TemperatureData td,
                      @NotNull Instant t) {
        category = Category.NOISE;
        data = td;
        this.t = t;
    }


    public SensorData(@NotNull HumidityData hd) {
        category = Category.HUMIDITY;
        data = hd;
        t = Instant.now();
    }

    public SensorData(@NotNull HumidityData hd,
                      @NotNull Instant t) {
        category = Category.NOISE;
        data = hd;
        this.t = t;
    }


    public SensorData(@NotNull LuminosityData ld) {
        category = Category.LUMINOSITY;
        data = ld;
        t = Instant.now();
    }

    public SensorData(@NotNull LuminosityData ld,
                      @NotNull Instant t) {
        category = Category.NOISE;
        data = ld;
        this.t = t;
    }


    public SensorData(@NotNull OtherData<?> od) {
        category = Category.OTHER;
        data = od;
        t = Instant.now();
    }

    public SensorData(@NotNull OtherData<?> od,
                      @NotNull Instant t) {
        category = Category.NOISE;
        data = od;
        this.t = t;
    }


    public Category getCategory() {
        return category;
    }

    /**
     * Provided convenience method. Check {@link Category} matches NOISE first.
     *
     * @return The noise data.
     * @throws ClassCastException   If the data does not match this type.
     */
    public @NotNull NoiseData getNoiseData() throws ClassCastException {
        return (NoiseData) data;
    }

    /**
     * Provided convenience method. Check {@link Category} matches TEMPERATURE first.
     *
     * @return The temperature data.
     * @throws ClassCastException   If the data does not match this type.
     */
    public TemperatureData getTemperatureData() throws ClassCastException {
        return (TemperatureData) data;
    }

    /**
     * Provided convenience method. Check {@link Category} matches HUMIDITY first.
     *
     * @return The humidity data.
     * @throws ClassCastException   If the data does not match this type.
     */

    public HumidityData getHumidityData() throws ClassCastException {
        return (HumidityData) data;
    }

    /**
     * Provided convenience method. Check {@link Category} matches LUMINOSITY first.
     *
     * @return The luminosity data.
     * @throws ClassCastException   If the data does not match this type.
     */
    public LuminosityData getLuminosityData() throws ClassCastException {
        return (LuminosityData) data;
    }

    /**
     * Provided convenience method. Check {@link Category} matches OTHER first.
     *
     * @return The other data.
     * @throws ClassCastException   If the data does not match this type.
     */
    public OtherData<? extends Serializable> getOtherData() throws ClassCastException {
        return (OtherData<? extends Serializable>) data;
    }

    public Instant getTimestamp() {
        return t;
    }

    @Override
    public long getApproximateSize() {
        long classSize = ClassLayout.parseClass(this.getClass()).instanceSize();
        if (category != null) {
            switch (category) {
                case NOISE:
                    return ((NoiseData) data).getApproximateSize() + classSize;
                case TEMPERATURE:
                    return ((TemperatureData) data).getApproximateSize() + classSize;
                case HUMIDITY:
                    return ((HumidityData) data).getApproximateSize() + classSize;
                case LUMINOSITY:
                    return ((LuminosityData) data).getApproximateSize() + classSize;
                case OTHER:
                    return ((OtherData<? extends Serializable>) data).getApproximateSize() + classSize;
            }
        }
        return classSize;
    }



    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("SensorData {");
        if (category != null) {
            switch (category) {
                case NOISE:
                    sb.append(((NoiseData) data).toString());
                    break;
                case TEMPERATURE:
                    sb.append(data).toString();
                    break;
                case HUMIDITY:
                    sb.append(data).toString();
                    break;
                case LUMINOSITY:
                    sb.append(data).toString();
                    break;
                case OTHER:
                    sb.append(data).toString();
                    break;
            }
        }
        sb.append(" }");
        return sb.toString();
    }
}
