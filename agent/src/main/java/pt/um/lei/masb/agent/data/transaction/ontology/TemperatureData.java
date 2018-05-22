package pt.um.lei.masb.agent.data.transaction.ontology;

import pt.um.lei.masb.blockchain.data.TUnit;

import java.util.Objects;

public class TemperatureData extends GeoData {
    private double temperature;
    private TUnit tUnit;

    public TemperatureData(double temperature,
                           TUnit tUnit,
                           String lat,
                           String lng) {
        super(lat, lng);
        this.temperature = temperature;
        this.tUnit = tUnit;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
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
        TemperatureData that = (TemperatureData) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                tUnit == that.tUnit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(temperature, tUnit);
    }

    @Override
    public String toString() {
        return "TemperatureData{" +
                "temperature=" + temperature +
                ", tUnit=" + tUnit +
                '}';
    }
}
