package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.TUnit;

import java.util.Objects;

public class JTemperatureData extends JGeoData {
    private String temperature;
    private TUnit tUnit;

    public JTemperatureData(String temperature,
                            TUnit tUnit,
                            String lat,
                            String lng) {
        super(lat, lng);
        this.temperature = temperature;
        this.tUnit = tUnit;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public TUnit gettUnit() {
        return tUnit;
    }

    public void settUnit(TUnit tUnit) {
        this.tUnit = tUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JTemperatureData that = (JTemperatureData) o;
        return Objects.equals(temperature, that.temperature) &&
                tUnit == that.tUnit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(temperature, tUnit);
    }

    @Override
    public String toString() {
        return "JTemperatureData{" +
                "temperature=" + temperature +
                ", tUnit=" + tUnit +
                '}';
    }
}
