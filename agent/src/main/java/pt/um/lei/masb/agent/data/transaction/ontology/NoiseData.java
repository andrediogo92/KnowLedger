package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.NUnit;

import java.util.Objects;

public class NoiseData extends GeoData {
    private double relativeOrRMS;
    private double peak;
    private NUnit unit;

    public NoiseData(double rms,
                     double peak,
                     String lat,
                     String lng) {
        super(lat, lng);
        this.relativeOrRMS = rms;
        this.peak = peak;
        this.unit = NUnit.RMS;
    }

    public double getRelativeOrRMS() {
        return relativeOrRMS;
    }

    public void setRelativeOrRMS(double relativeOrRMS) {
        this.relativeOrRMS = relativeOrRMS;
    }

    public double getPeak() {
        return peak;
    }

    public void setPeak(double peak) {
        this.peak = peak;
    }

    public NUnit getUnit() {
        return unit;
    }

    public void setUnit(NUnit unit) {
        this.unit = unit;
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
        return Double.compare(noiseData.relativeOrRMS, relativeOrRMS) == 0 &&
                Double.compare(noiseData.peak, peak) == 0 &&
                unit == noiseData.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(relativeOrRMS, peak, unit);
    }

    @Override
    public String toString() {
        return "NoiseData{" +
                "relativeOrRMS=" + relativeOrRMS +
                ", peak=" + peak +
                ", unit=" + unit +
                '}';
    }
}
