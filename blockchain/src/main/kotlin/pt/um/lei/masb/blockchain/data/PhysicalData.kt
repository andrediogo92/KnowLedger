package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.ledger.BlockChainContract
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.Sizeable
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
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
    Storable,
    BlockChainContract,
    DataCategory by data,
    SelfInterval by data {

    override val approximateSize: Long
        get() = ClassLayout
            .parseClass(this::class.java)
            .instanceSize() + data.approximateSize

    constructor(
        data: BlockChainData
    ) : this(
        Instant.now(),
        null,
        data
    )

    constructor(
        instant: Instant,
        data: BlockChainData
    ) : this(
        instant,
        null, data
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

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("PhysicalData")
            .apply {
                this.setProperty(
                    "seconds",
                    instant.epochSecond
                )
                this.setProperty(
                    "nanos",
                    instant.nano
                )
                this.setProperty(
                    "data",
                    data.store(session)
                )
                geoCoords?.let {
                    this.setProperty(
                        "latitude",
                        it.latitude
                    )
                    this.setProperty(
                        "longitude",
                        it.longitude
                    )
                    this.setProperty(
                        "altitude",
                        it.altitude
                    )
                }
            }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
                ${instant.epochSecond}
                ${instant.nano}
                ${geoCoords?.latitude}
                ${geoCoords?.longitude}
                ${geoCoords?.altitude}
                $data
            """.trimIndent()
        )

    override fun toString(): String = """
        |           Physical Data: {
        |               Instant: $instant
        |               Latitude: ${geoCoords?.latitude}
        |               Longitude: ${geoCoords?.longitude}
        |               Altitude: ${geoCoords?.altitude}
        |               Payload: $data
        |           }
        """.trimMargin()

    companion object {
        val MATH_CONTEXT = MathContext(
            15,
            RoundingMode.HALF_EVEN
        )
    }
}