package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.Sizeable
import org.knowledger.ledger.core.data.DataCategory
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.flattenBytes
import org.knowledger.ledger.core.storage.LedgerContract
import org.openjdk.jol.info.ClassLayout
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
    SelfInterval by data,
    Comparable<PhysicalData> {
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

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, alt: BigDecimal,
        data: LedgerData
    ) : this(instant, GeoCoords(lat, lng, alt), data)

    override fun compareTo(other: PhysicalData): Int =
        when {
            instant > other.instant -> -1
            instant < other.instant -> 1
            else -> 0
        }

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