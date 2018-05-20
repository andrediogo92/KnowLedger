package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Temperature data specifies a double and a Temperature unit (Celsius, Fahrenheit, Rankine and Kelvin) and
 * idempotent methods to convert between them as needed.
 */
@Entity
public final class TemperatureData extends GeoData implements Sizeable {

    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private final double temperature;

    @Basic(optional = false)
    private final TUnit unit;


    protected TemperatureData() {
        super(new BigDecimal(0), new BigDecimal(0));
        temperature = 0;
        unit = null;
    }


    public TemperatureData(double temperature,
                           @NotNull TUnit unit,
                           BigDecimal lat,
                           BigDecimal lng) {
        super(lat, lng);
        this.temperature = temperature;
        this.unit = unit;

    }


    /**
     * @return The temperature reading's unit (Celsius, Fahrenheit, Rankine, Kelvin).
     */
    public TUnit getUnit() {
        return unit;
    }

    /**
     * @return A temperature reading in a certain unit.
     */
    public double getTemperature() {
        return temperature;
    }


    public double convertToCelsius() {
        var res = temperature;
        switch(unit) {
            case CELSIUS:
                break;
            case FAHRENHEIT:
                res = (5 * (res - 32)) / 9.0;
                break;
            case KELVIN:
                res -= 273.16;
                break;
            case RANKINE:
                res = (5 * (res - 459.69 - 32)) / 9.0;
                break;
        }
        return res;
    }

    public double convertToFahrenheit() {
        var res = temperature;
        switch(unit) {
            case CELSIUS:
                res = (9 * (res / 5.0)) + 32;
                break;
            case FAHRENHEIT:
                break;
            case KELVIN:
                res = (9 * (res - 273.15) / 5.0) + 32;
                break;
            case RANKINE:
                res -= 459.69;
                break;
        }
        return res;
    }

    public double convertToRankine() {
        var res = temperature;
        switch(unit) {
            case CELSIUS:
                res = (9 * res / 5.0) + 459.69 + 32;
                break;
            case FAHRENHEIT:
                res += 459.69;
                break;
            case KELVIN:
                res = (9 * (res - 273.15) / 5.0) + 32 + 459.69;
            case RANKINE: break;
        }
        return res;
    }

    public double convertToKelvin() {
        var res = temperature;
        switch (unit){
            case CELSIUS:
                res += 273.15;
                break;
            case FAHRENHEIT:
                res = (5.0 * (res - 32) / 9.0) + 273.15;
                break;
            case KELVIN:
                break;
            case RANKINE:
                res = (5 * (res - 459.69 - 32)) / 9.0 + 273.15;
                break;
        }
        return res;
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
