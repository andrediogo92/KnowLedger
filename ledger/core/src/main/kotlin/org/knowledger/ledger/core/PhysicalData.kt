@file:UseSerializers(GeoCoordinatesSerializer::class)

package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.data.DataCategory
import org.knowledger.ledger.core.data.GeoCoords
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.serial.GeoCoordinatesSerializer
import java.math.BigDecimal

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
    val data: LedgerData,
) : HashSerializable, Cloneable, DataCategory by data, SelfInterval by data,
    Comparable<PhysicalData>, LedgerContract {
    public override fun clone(): PhysicalData =
        copy(millis = millis, coords = coords, data = data.clone())

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(serializer(), this)

    constructor(
        geoCoords: GeoCoords, data: LedgerData,
    ) : this(nowUTC().toEpochMilliseconds(), geoCoords, data)

    constructor(
        lat: BigDecimal, lng: BigDecimal, data: LedgerData,
    ) : this(nowUTC().toEpochMilliseconds(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal, lng: BigDecimal, data: LedgerData,
    ) : this(instant.toEpochMilliseconds(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal, lng: BigDecimal, alt: BigDecimal, data: LedgerData,
    ) : this(instant.toEpochMilliseconds(), GeoCoords(lat, lng, alt), data)

    override fun compareTo(other: PhysicalData): Int =
        millis.compareTo(other.millis)
}