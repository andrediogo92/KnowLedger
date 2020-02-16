package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.hash.Hash
import kotlin.properties.Delegates

internal abstract class AbstractCoinbaseParamsSerializer : KSerializer<CoinbaseParams>,
                                                           HashEncode {
    private object CoinbaseParamsSerialDescriptor : SerialClassDescImpl("CoinbaseParams") {
        init {
            addElement("timeIncentive")
            addElement("valueIncentive")
            addElement("baseIncentive")
            addElement("dividingThreshold")
            addElement("formula")
        }
    }

    override val descriptor: SerialDescriptor = CoinbaseParamsSerialDescriptor

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
                    4 -> formula = decodeHash(i)
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
            encodeHash(4, obj.formula)
            endStructure(descriptor)
        }
    }
}