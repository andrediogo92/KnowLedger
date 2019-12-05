package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.serial.CoinbaseParamsSerializer
import kotlin.properties.Delegates

@Serializer(forClass = CoinbaseParams::class)
object CoinbaseParamsByteSerializer : KSerializer<CoinbaseParams> {
    override val descriptor: SerialDescriptor =
        CoinbaseParamsSerializer.descriptor

    override fun deserialize(decoder: Decoder): CoinbaseParams =
        with(decoder.beginStructure(descriptor)) {
            var timeIncentive by Delegates.notNull<Long>()
            var valueIncentive by Delegates.notNull<Long>()
            var baseIncentive by Delegates.notNull<Long>()
            var dividingThreshold by Delegates.notNull<Long>()
            lateinit var formula: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> timeIncentive = decodeLongElement(
                        descriptor, i
                    )
                    1 -> valueIncentive = decodeLongElement(
                        descriptor, i
                    )
                    2 -> baseIncentive = decodeLongElement(
                        descriptor, i
                    )
                    3 -> dividingThreshold = decodeLongElement(
                        descriptor, i
                    )
                    4 -> formula = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            CoinbaseParams(
                timeIncentive = timeIncentive,
                valueIncentive = valueIncentive,
                baseIncentive = baseIncentive,
                dividingThreshold = dividingThreshold,
                formula = formula
            )
        }

    override fun serialize(encoder: Encoder, obj: CoinbaseParams) {
        with(encoder.beginStructure(descriptor)) {
            encodeLongElement(
                descriptor, 0, obj.timeIncentive
            )
            encodeLongElement(
                descriptor, 1, obj.valueIncentive
            )
            encodeLongElement(
                descriptor, 2, obj.baseIncentive
            )
            encodeLongElement(
                descriptor, 3, obj.dividingThreshold
            )
            encodeSerializableElement(
                descriptor, 4, HashSerializer, obj.formula
            )
            endStructure(descriptor)
        }
    }
}