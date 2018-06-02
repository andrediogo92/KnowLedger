package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.HUnit;

import java.util.Objects;

public class JHumidityData extends JGeoData {
    private String hum;
    private HUnit unit;

    public JHumidityData(String hum,
                         HUnit unit,
                         String lat,
                         String lng) {
        super(lat, lng);
        this.hum = hum;
        this.unit = unit;
    }

    public String getHum() {
        return hum;
    }

    public void setHum(String hum) {
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
        JHumidityData that = (JHumidityData) o;
        return Objects.equals(hum, that.hum) &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(hum, unit);
    }

    @Override
    public String toString() {
        return "JHumidityData{" +
                "hum=" + hum +
                ", unit=" + unit +
                '}';
    }
}
