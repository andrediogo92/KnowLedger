package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.utils.GeoCoords;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Data constrained to some latitude and longitude coordinates.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class GeoData {
    @Id
    @GeneratedValue
    private long id;

    @Embedded
    private final GeoCoords gc;


    protected GeoData() {
        gc = new GeoCoords();
    }

    GeoData(@DecimalMin(value = "-90")
            @DecimalMax(value = "90")
                    BigDecimal lat,
            @DecimalMin(value = "-180")
            @DecimalMax(value = "180")
                    BigDecimal lng) {
        gc = new GeoCoords(lat, lng);
    }

    /**
     * @return Latitude for this data object.
     */
    public BigDecimal getLatitude() {
        return gc.getLatitude();
    }

    /**
     * @return Longitude for this data object.
     */
    public BigDecimal getLongitude() {
        return gc.getLongitude();
    }
}
