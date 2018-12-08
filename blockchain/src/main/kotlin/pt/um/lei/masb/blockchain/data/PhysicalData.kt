package pt.um.lei.masb.blockchain.data

import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.Sizeable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.GeoCoords
import pt.um.lei.masb.blockchain.utils.Hashable
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant

/**
 * Physical data is the main class in which to store blockchain data.
 *
 * It requires an instant in which the data was recorded and
 * optionally geo coordinates for where it was recorded.
 */
data class PhysicalData(
    val instant: Instant,
    val geoCoords: GeoCoords?,
    val data: BlockChainData
) : Sizeable,
    Hashable,
    DataCategory by data,
    SelfInterval by data {

    override val approximateSize: Long
        get() = ClassLayout
            .parseClass(this::class.java)
            .instanceSize() + data.approximateSize

    constructor(data: BlockChainData) : this(
        Instant.now(),
        null,
        data
    )

    constructor(
        geoCoords: GeoCoords,
        data: BlockChainData
    ) : this(
        Instant.now(),
        geoCoords,
        data
    )

    constructor(
        lat: BigDecimal,
        lng: BigDecimal,
        data: BlockChainData
    ) : this(
        Instant.now(),
        GeoCoords(lat, lng),
        data
    )

    constructor(
        instant: Instant,
        lat: BigDecimal,
        lng: BigDecimal,
        data: BlockChainData
    ) : this(
        instant,
        GeoCoords(lat, lng),
        data
    )

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
                $instant
                ${geoCoords?.latitude}
                ${geoCoords?.longitude}
                ${geoCoords?.altitude}
                $data
            """.trimIndent()
        )

    override fun toString(): String = """
        |           Physical Data: {
        |               $instant
        |               ${geoCoords?.latitude}
        |               ${geoCoords?.longitude}
        |               ${geoCoords?.altitude}
        |               $data
        |           }
        """.trimMargin()

    companion object {
        val MATH_CONTEXT = MathContext(
            15,
            RoundingMode.HALF_EVEN
        )
    }
}