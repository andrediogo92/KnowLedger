package pt.um.lei.masb.blockchain.utils

import java.math.BigDecimal

data class GeoCoords(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val altitude: BigDecimal = BigDecimal.ZERO
) {

    override fun toString(): String = """
        | GeoCoords{
        |   latitude=$latitude,
        |   longitude=$longitude,
        |   altitude=$altitude
        | }
        """.trimMargin()


}
