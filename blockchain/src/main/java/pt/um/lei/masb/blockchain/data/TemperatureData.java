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
    private final BigDecimal temperature;

    @Basic(optional = false)
    private final TUnit unit;


    protected TemperatureData() {
        super(new BigDecimal(0), new BigDecimal(0));
        temperature = null;
        unit = null;
    }


    public TemperatureData(BigDecimal temperature,
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
    public BigDecimal getTemperature() {
        return temperature;
    }


    public BigDecimal convertToCelsius() {
        BigDecimal res = temperature;
        switch(unit) {
            case CELSIUS:
                break;
            case FAHRENHEIT:
                res = new BigDecimal("5").multiply(res.subtract(new BigDecimal("32")))
                                         .divide(new BigDecimal("9"),
                                                 SensorData.getMathContext());
                break;
            case KELVIN:
                res = res.subtract(new BigDecimal("273.16"));
                break;
            case RANKINE:
                res = new BigDecimal("5").multiply(res.subtract(new BigDecimal("491.69")))
                                         .divide(new BigDecimal("9"),
                                                 SensorData.getMathContext());
                break;
        }
        return res;
    }

    public BigDecimal convertToFahrenheit() {
        BigDecimal res = temperature;
        switch(unit) {
            case CELSIUS:
                res = new BigDecimal("9").multiply(res.divide(new BigDecimal("5")))
                                         .add(new BigDecimal("32"));
                break;
            case FAHRENHEIT:
                break;
            case KELVIN:
                res = new BigDecimal("9").multiply(res.subtract(new BigDecimal("273.15")))
                                         .divide(new BigDecimal("5.0"),
                                                 SensorData.getMathContext())
                                         .add(new BigDecimal("32"));
                break;
            case RANKINE:
                res = res.subtract(new BigDecimal("459.69"));
                break;
        }
        return res;
    }

    public BigDecimal convertToRankine() {
        BigDecimal res = temperature;
        switch(unit) {
            case CELSIUS:
                res = new BigDecimal("9").multiply(res.divide(new BigDecimal("5"),
                                                              SensorData.getMathContext()))
                                         .add(new BigDecimal("491.69"));
                break;
            case FAHRENHEIT:
                res = res.add(new BigDecimal("459.69"));
                break;
            case KELVIN:
                res = new BigDecimal("9").multiply(res.subtract(new BigDecimal("273.15")))
                                         .divide(new BigDecimal("5"))
                                         .add(new BigDecimal("491.69"));
            case RANKINE: break;
        }
        return res;
    }

    public BigDecimal convertToKelvin() {
        BigDecimal res = temperature;
        switch (unit){
            case CELSIUS:
                res = res.add(new BigDecimal("273.15"));
                break;
            case FAHRENHEIT:
                res = new BigDecimal("5").multiply(res.subtract(new BigDecimal("32")))
                                         .divide(new BigDecimal("9"),
                                                 SensorData.getMathContext())
                                         .add(new BigDecimal("273.15"));
                break;
            case KELVIN:
                break;
            case RANKINE:
                res = new BigDecimal("5").multiply(res.subtract(new BigDecimal("491.69")))
                                         .divide(new BigDecimal("9"),
                                                 SensorData.getMathContext())
                                         .add(new BigDecimal("273.15"));
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
        return id == that.id &&
                Objects.equals(temperature, that.temperature) &&
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
          .append("º");
        switch (unit) {
            case KELVIN: sb.append('K');break;
            case CELSIUS: sb.append('C');break;
            case RANKINE: sb.append('R');break;
            case FAHRENHEIT: sb.append('F');break;
        }
        return sb.append(" }").toString();
    }

}
