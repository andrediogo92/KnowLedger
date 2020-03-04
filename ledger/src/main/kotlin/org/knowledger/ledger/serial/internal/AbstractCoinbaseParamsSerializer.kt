package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import kotlin.properties.Delegates

internal abstract class AbstractCoinbaseParamsSerializer : KSerializer<CoinbaseParams>,
                                                           HashEncode {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("CoinbaseParams") {
            val timeIncentive = PrimitiveDescriptor(
                "timeIncentive", PrimitiveKind.LONG
            )
            val valueIncentive = PrimitiveDescriptor(
                "valueIncentive", PrimitiveKind.LONG
            )
            val baseIncentive = PrimitiveDescriptor(
                "baseIncentive", PrimitiveKind.LONG
            )
            val dividingThreshold = PrimitiveDescriptor(
                "dividingThreshold", PrimitiveKind.LONG
            )
            element(
                elementName = timeIncentive.serialName,
                descriptor = timeIncentive
            )
            element(
                elementName = valueIncentive.serialName,
                descriptor = valueIncentive
            )
            element(
                elementName = baseIncentive.serialName,
                descriptor = baseIncentive
            )
            element(
                elementName = dividingThreshold.serialName,
                descriptor = dividingThreshold
            )
            element(
                elementName = "formula",
                descriptor = hashDescriptor
            )
        }

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

    override fun serialize(encoder: Encoder, value: CoinbaseParams) {
        with(encoder.beginStructure(descriptor)) {
            encodeLongElement(
                descriptor, 0, value.timeIncentive
            )
            encodeLongElement(
                descriptor, 1, value.valueIncentive
            )
            encodeLongElement(
                descriptor, 2, value.baseIncentive
            )
            encodeLongElement(
                descriptor, 3, value.dividingThreshold
            )
            encodeHash(4, value.formula)
            endStructure(descriptor)
        }
    }
}