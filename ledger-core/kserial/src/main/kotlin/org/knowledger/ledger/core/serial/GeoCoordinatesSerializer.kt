package org.knowledger.ledger.core.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.base.data.GeoCoords
import java.math.BigDecimal

@Serializer(forClass = GeoCoords::class)
object GeoCoordinatesSerializer : KSerializer<GeoCoords> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("GeoCoordinates") {
            init {
                addElement(name = "latitude")
                addElement(name = "longitude")
                addElement(name = "altitude", isOptional = true)
            }
        }

    override fun deserialize(decoder: Decoder): GeoCoords {
        return with(decoder.beginStructure(descriptor)) {
            lateinit var latitude: BigDecimal
            lateinit var longitude: BigDecimal
            var altitude: BigDecimal? = null
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> latitude = decodeSerializableElement(
                        descriptor, i, BigDecimalSerializer
                    )
                    1 -> longitude = decodeSerializableElement(
                        descriptor, i, BigDecimalSerializer
                    )
                    2 -> altitude = decodeSerializableElement(
                        descriptor, i, BigDecimalSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            if (altitude == null) {
                GeoCoords(latitude, longitude)
            } else {
                GeoCoords(latitude, longitude, altitude)
            }
        }
    }

    override fun serialize(encoder: Encoder, obj: GeoCoords) =
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(descriptor, 0, BigDecimalSerializer, obj.latitude)
            encodeSerializableElement(descriptor, 1, BigDecimalSerializer, obj.longitude)
            encodeSerializableElement(descriptor, 2, BigDecimalSerializer, obj.altitude)
            endStructure(descriptor)
        }
}

