package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.HUnit;

import java.util.Objects;

public class HumidityData extends GeoData {
    private double hum;
    private HUnit unit;

    public HumidityData(double hum,
                        HUnit unit,
                        String lat,
                        String lng) {
        super(lat, lng);
        this.hum = hum;
        this.unit = unit;
    }

    public double getHum() {
        return hum;
    }

    public void setHum(double hum) {
        this.hum = hum;
    }

    public HUnit getUnit() {
        return unit;
    }

    public void setUnit(HUnit unit) {
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
        HumidityData that = (HumidityData) o;
        return Double.compare(that.hum, hum) == 0 &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(hum, unit);
    }

    @Override
    public String toString() {
        return "HumidityData{" +
                "hum=" + hum +
                ", unit=" + unit +
                '}';
    }
}
