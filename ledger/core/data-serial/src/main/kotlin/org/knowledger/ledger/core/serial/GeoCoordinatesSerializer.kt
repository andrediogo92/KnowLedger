@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.data.GeoCoords
import java.math.BigDecimal

@Serializer(forClass = GeoCoords::class)
object GeoCoordinatesSerializer : KSerializer<GeoCoords> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("GeoCoordinates") {
            element(elementName = "latitude", descriptor = BigDecimalSerializer.descriptor)
            element(elementName = "longitude", descriptor = BigDecimalSerializer.descriptor)
            element(
                elementName = "altitude", descriptor = BigDecimalSerializer.descriptor,
                isOptional = true
            )
        }

    override fun deserialize(decoder: Decoder): GeoCoords =
        compositeDecode(decoder) {
            lateinit var latitude: BigDecimal
            lateinit var longitude: BigDecimal
            var altitude: BigDecimal? = null
            while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break
                    0 -> latitude = decodeSerializableElement(descriptor, i, BigDecimalSerializer)
                    1 -> longitude = decodeSerializableElement(descriptor, i, BigDecimalSerializer)
                    2 -> altitude = decodeSerializableElement(descriptor, i, BigDecimalSerializer)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            if (altitude == null) {
                GeoCoords(latitude, longitude)
            } else {
                GeoCoords(latitude, longitude, altitude)
            }
        }

    override fun serialize(encoder: Encoder, value: GeoCoords) {
        compositeEncode(encoder) {
            encodeSerializableElement(descriptor, 0, BigDecimalSerializer, value.latitude)
            encodeSerializableElement(descriptor, 1, BigDecimalSerializer, value.longitude)
            encodeSerializableElement(descriptor, 2, BigDecimalSerializer, value.altitude)
        }
    }
}

