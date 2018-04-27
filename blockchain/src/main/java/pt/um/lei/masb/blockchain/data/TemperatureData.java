package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class TemperatureData implements Sizeable {

    private double temperature;
    private TUnit unit;

    public TemperatureData(double temperature, @NotNull TUnit unit) {
        this.temperature = temperature;
        this.unit = unit;
    }

    protected TemperatureData() {}

    public TUnit getUnit() {
        return unit;
    }

    public double getTemperature() {
        return temperature;
    }


    public void convertToCelsius() {
        switch(unit) {
            case CELSIUS: break;
            case FAHRENHEIT: temperature = (5 * (temperature - 32)) /9.0;
                    unit = TUnit.CELSIUS;
                    break;
            case KELVIN: temperature -= 273.16;
                    unit = TUnit.CELSIUS;
                    break;
            case RANKINE: temperature = (5 * (temperature - 459.69 - 32)) / 9.0;
                    unit = TUnit.CELSIUS;
                    break;
        }
    }

    public void convertToFahrenheit() {
        switch(unit) {
            case CELSIUS: temperature = (9 * (temperature/ 5.0)) + 32;
                    unit = TUnit.FAHRENHEIT;
                    break;
            case FAHRENHEIT: break;
            case KELVIN: temperature = (9 * (temperature - 273.15) / 5.0) + 32;
                    unit = TUnit.FAHRENHEIT;
                    break;
            case RANKINE: temperature -= temperature;
                    unit = TUnit.FAHRENHEIT;
                    break;
        }
    }

    public void convertToRankine() {
        switch(unit) {
            case CELSIUS: temperature = (9 * temperature / 5.0) + 459.69 + 32;
                    unit = TUnit.RANKINE;
                    break;
            case FAHRENHEIT: temperature += temperature;
                    unit = TUnit.RANKINE;
                    break;
            case KELVIN: temperature = (9 * (temperature - 273.15) / 5.0) + 32 + 459.69;
                    unit = TUnit.RANKINE;
            case RANKINE: break;
        }
    }

    public void convertToKelvin() {
        switch (unit){
            case CELSIUS: temperature += 273.15;
                    unit = TUnit.KELVIN;
                    break;
            case FAHRENHEIT: temperature = (5.0 * (temperature - 32) / 9.0) + 273.15;
                    unit = TUnit.KELVIN;
            case KELVIN: break;
            case RANKINE: temperature = (5 * (temperature - 459.69 - 32)) / 9.0 + 273.15;
                    unit = TUnit.KELVIN;
                    break;
        }
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
                unit == that.unit;
    }

    @Override
    public int hashCode() {

        return Objects.hash(temperature, unit);
    }


    @Override
    public @NotEmpty String toString() {
        var sb = new StringBuilder();
        sb.append("TemperatureData {")
          .append(temperature)
          .append('º');
        switch (unit) {
            case KELVIN: sb.append('K');break;
            case CELSIUS: sb.append('C');break;
            case RANKINE: sb.append('R');break;
            case FAHRENHEIT: sb.append('F');break;
        }
        return sb.append(" }").toString();
    }

}
