package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.LUnit;

import java.util.Objects;


public class JLuminosityData extends JGeoData {
    private double lum;
    private LUnit unit;

    public JLuminosityData(double lum,
                           LUnit unit,
                           String lat,
                           String lng) {
        super(lat, lng);
        this.lum = lum;
        this.unit = unit;
    }

    public double getLum() {
        return lum;
    }

    public void setLum(double lum) {
        this.lum = lum;
    }

    public LUnit getUnit() {
        return unit;
    }

    public void setUnit(LUnit unit) {
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
        JLuminosityData that = (JLuminosityData) o;
        return Double.compare(that.lum, lum) == 0 &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(lum, unit);
    }

    @Override
    public String toString() {
        return "JLuminosityData{" +
                "lum=" + lum +
                ", unit=" + unit +
                '}';
    }
}
