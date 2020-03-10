@file:UseSerializers(GeoCoordinatesSerializer::class)

package org.knowledger.ledger.core.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.base.data.DataCategory
import org.knowledger.ledger.core.base.data.GeoCoords
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.core.base.data.SelfInterval
import org.knowledger.ledger.core.base.serial.HashSerializable
import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.core.serial.GeoCoordinatesSerializer
import java.math.BigDecimal
import java.time.Instant

/**
 * Physical value is the main class in which to store ledger value.
 *
 * It requires the milli seconds from Unix Epoch ([millis]) in which
 * the value was recorded and optional geo coordinates for where it
 * was recorded.
 */
@Serializable
data class PhysicalData(
    val millis: Long,
    val coords: GeoCoords,
    val data: LedgerData
) : HashSerializable,
    Cloneable,
    DataCategory by data,
    SelfInterval by data,
    Comparable<PhysicalData>,
    LedgerContract {
    public override fun clone(): PhysicalData =
        PhysicalData(
            millis, coords, data.clone()
        )

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    constructor(
        geoCoords: GeoCoords, data: LedgerData
    ) : this(Instant.now().toEpochMilli(), geoCoords, data)

    constructor(
        lat: BigDecimal, lng: BigDecimal,
        data: LedgerData
    ) : this(Instant.now().toEpochMilli(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, data: LedgerData
    ) : this(instant.toEpochMilli(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, alt: BigDecimal,
        data: LedgerData
    ) : this(instant.toEpochMilli(), GeoCoords(lat, lng, alt), data)

    override fun compareTo(other: PhysicalData): Int =
        millis.compareTo(other.millis)
}