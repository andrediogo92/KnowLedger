package pt.um.lei.masb.blockchain.utils

import java.math.BigDecimal

class GeoCoords(
        val latitude: BigDecimal,
        val longitude: BigDecimal
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeoCoords) return false

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    override fun toString(): String =
            "GeoCoords{latitude=$latitude, longitude=$longitude}"


}
