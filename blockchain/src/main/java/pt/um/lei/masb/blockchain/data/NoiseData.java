package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Coinbase;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;


/**
 * Ambient Noise Level measured in dB.
 */
@Entity
public final class NoiseData extends GeoData implements Sizeable, SelfInterval<NoiseData> {
    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private final BigDecimal relativeOrRMS;

    private final BigDecimal peak;

    @Basic(optional = false)
    private final NUnit unit;

    protected NoiseData() {
        super(new BigDecimal(0), new BigDecimal(0));
        relativeOrRMS = null;
        peak = null;
        unit = null;
    }

    public NoiseData(BigDecimal rms,
                     BigDecimal peak,
                     BigDecimal lat,
                     BigDecimal lng) {
        super(lat, lng);
        this.relativeOrRMS = rms;
        this.peak = peak;
        this.unit = NUnit.RMS;
    }

    public NoiseData(BigDecimal relativeOrRMS,
                     BigDecimal lat,
                     BigDecimal lng) {
        super(lat, lng);
        this.relativeOrRMS = relativeOrRMS;
        this.peak = null;
        this.unit = NUnit.DBSPL;
    }

    @Override
    public @NotNull BigDecimal calculateDiff(@NotNull NoiseData oldND) {
        var newN = relativeOrRMS.add(peak).abs();
        var oldN = oldND.getNoiseLevel().add(oldND.getPeakOrBase()).abs();
        return newN.subtract(oldN)
                   .divide(oldN, Coinbase.getMathContext());
    }



    /**
     * @return A noise level either as an
     * RMS sampled from a PCM signal in the interval [-1, 1],
     * or a dB relative to the standard base ().
     */
    public BigDecimal getNoiseLevel() {
        return this.relativeOrRMS;
    }

    /**
     * @return The peak of the PCM signal.
     */
    public BigDecimal getPeakOrBase() {
        return peak;
    }

    /**
     * @return The unit for the measurement.
     */
    public NUnit getUnit() {
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
        NoiseData noiseData = (NoiseData) o;
        return id == noiseData.id &&
                Objects.equals(relativeOrRMS, noiseData.relativeOrRMS) &&
                Objects.equals(peak, noiseData.peak) &&
                unit == noiseData.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, relativeOrRMS, peak, unit);
    }

    @Override
    public String toString() {
        return "NoiseData{" +
                "id=" + id +
                ", relativeOrRMS=" + relativeOrRMS +
                ", peak=" + peak +
                ", unit=" + unit +
                '}';
    }
}
