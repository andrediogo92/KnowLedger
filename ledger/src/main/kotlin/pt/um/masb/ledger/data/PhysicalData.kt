package pt.um.masb.ledger.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Hash
import pt.um.masb.common.Hashable
import pt.um.masb.common.Sizeable
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.DataCategory
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.LedgerContract
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant

/**
 * Physical data is the main class in which to store ledger data.
 *
 * It requires an [instant] in which the data was recorded and
 * optionally geo coordinates for where it was recorded.
 */
@JsonClass(generateAdapter = true)
data class PhysicalData(
    val instant: Instant,
    val geoCoords: GeoCoords?,
    val data: BlockChainData
) : Sizeable,
    Hashable,
    Storable,
    LedgerContract,
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
                setProperty(
                    "seconds",
                    instant.epochSecond
                )
                setProperty(
                    "nanos",
                    instant.nano
                )
                setProperty(
                    "data",
                    data.store(session)
                )
                geoCoords?.let {
                    setProperty(
                        "latitude",
                        it.latitude
                    )
                    setProperty(
                        "longitude",
                        it.longitude
                    )
                    setProperty(
                        "altitude",
                        it.altitude
                    )
                }
            }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            if (geoCoords != null) {
                flattenBytes(
                    instant.epochSecond.bytes(),
                    instant.nano.bytes(),
                    geoCoords.latitude.unscaledValue().toByteArray(),
                    geoCoords.longitude.unscaledValue().toByteArray(),
                    geoCoords.altitude.unscaledValue().toByteArray(),
                    data.digest(c)
                )
            } else {
                flattenBytes(
                    instant.epochSecond.bytes(),
                    instant.nano.bytes(),
                    data.digest(c)
                )
            }
        )

    companion object {
        val MATH_CONTEXT = MathContext(
            15,
            RoundingMode.HALF_EVEN
        )
    }
}