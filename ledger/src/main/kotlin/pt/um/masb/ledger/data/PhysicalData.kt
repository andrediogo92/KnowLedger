package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Sizeable
import pt.um.masb.common.data.DataCategory
import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant

/**
 * Physical value is the main class in which to store ledger value.
 *
 * It requires an [instant] in which the value was recorded and
 * optionally geo coordinates for where it was recorded.
 */
@JsonClass(generateAdapter = true)
data class PhysicalData(
    val instant: Instant,
    val geoCoords: GeoCoords?,
    val data: LedgerData
) : Sizeable,
    Hashable,
    LedgerContract,
    DataCategory by data,
    SelfInterval by data {

    override val approximateSize: Long
        get() = ClassLayout
            .parseClass(this::class.java)
            .instanceSize() + data.approximateSize

    constructor(
        data: LedgerData
    ) : this(Instant.now(), null, data)

    constructor(
        instant: Instant, data: LedgerData
    ) : this(instant, null, data)

    constructor(
        geoCoords: GeoCoords, data: LedgerData
    ) : this(Instant.now(), geoCoords, data)

    constructor(
        lat: BigDecimal, lng: BigDecimal,
        data: LedgerData
    ) : this(Instant.now(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, data: LedgerData
    ) : this(instant, GeoCoords(lat, lng), data)

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            if (geoCoords != null) {
                flattenBytes(
                    instant.bytes(),
                    geoCoords.latitude.bytes(),
                    geoCoords.longitude.bytes(),
                    geoCoords.altitude.bytes(),
                    data.digest(c).bytes
                )
            } else {
                flattenBytes(
                    instant.bytes(),
                    data.digest(c).bytes
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