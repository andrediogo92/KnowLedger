package pt.um.lei.masb.agent.messaging.transaction.ontology;

import pt.um.lei.masb.blockchain.data.NUnit;

import java.util.Objects;

public class JNoiseData extends JGeoData {
    private String relativeOrRMS;
    private String peak;
    private NUnit unit;

    public JNoiseData(String rms,
                      String peak,
                      NUnit nUnit,
                      String lat,
                      String lng) {
        super(lat, lng);
        this.relativeOrRMS = rms;
        this.peak = peak;
        this.unit = nUnit;
    }


    public String getRelativeOrRMS() {
        return relativeOrRMS;
    }

    public void setRelativeOrRMS(String relativeOrRMS) {
        this.relativeOrRMS = relativeOrRMS;
    }

    public String getPeak() {
        return peak;
    }

    public void setPeak(String peak) {
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
        JNoiseData that = (JNoiseData) o;
        return Objects.equals(relativeOrRMS, that.relativeOrRMS) &&
                Objects.equals(peak, that.peak) &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(relativeOrRMS, peak, unit);
    }

    @Override
    public String toString() {
        return "JNoiseData{" +
                "relativeOrRMS=" + relativeOrRMS +
                ", peak=" + peak +
                ", unit=" + unit +
                '}';
    }
}
