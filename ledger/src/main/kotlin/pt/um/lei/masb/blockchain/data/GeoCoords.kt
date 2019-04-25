package pt.um.lei.masb.blockchain.data

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

/**
 * GeoCoords represent coordinates with optional altitude
 * defaulted to sea level or altitude 0.
 */
@JsonClass(generateAdapter = true)
data class GeoCoords(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val altitude: BigDecimal = BigDecimal.ZERO
)
