package pt.um.lei.masb.blockchain.utils;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public final class GeoCoords {
    @Basic(optional = false)
    private BigDecimal latitude;

    @Basic(optional = false)
    private BigDecimal longitude;

    public GeoCoords() {
    }

    public GeoCoords(@DecimalMin(value = "-90")
                     @DecimalMax(value = "90")
                             BigDecimal latitude,
                     @DecimalMin(value = "-180")
                     @DecimalMax(value = "180")
                             BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoCoords geoCoords = (GeoCoords) o;
        return Objects.equals(latitude, geoCoords.latitude) &&
                Objects.equals(longitude, geoCoords.longitude);
    }

    @Override
    public int hashCode() {

        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "GeoCoords{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
