package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import kotlin.properties.Delegates

internal abstract class AbstractCoinbaseSerializer(
    witnessSerializer: KSerializer<Witness>
) : KSerializer<Coinbase>, HashEncode {
    private val witnessesSerializer =
        SortedListSerializer(witnessSerializer)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Coinbase") {
            val blockheight = PrimitiveDescriptor(
                "blockheight", PrimitiveKind.LONG
            )
            val extraNonce = PrimitiveDescriptor(
                "extraNonce", PrimitiveKind.LONG
            )
            element(
                elementName = "hash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "payout",
                descriptor = PayoutSerializer.descriptor
            )
            element(
                elementName = "difficulty",
                descriptor = difficultyDescriptor
            )
            element(
                elementName = blockheight.serialName,
                descriptor = blockheight
            )
            element(
                elementName = extraNonce.serialName,
                descriptor = extraNonce
            )
            element(
                elementName = "coinbaseParams",
                descriptor = coinbaseParamsDescriptor
            )
            element(
                elementName = "witnesses",
                descriptor = witnessesSerializer.descriptor
            )
        }

    abstract val difficultyDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeDifficulty(
        index: Int, difficulty: Difficulty
    )

    abstract fun CompositeDecoder.decodeDifficulty(
        index: Int
    ): Difficulty

    abstract val coinbaseParamsDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeCoinbaseParams(
        index: Int, params: CoinbaseParams
    )

    abstract fun CompositeDecoder.decodeCoinbaseParams(
        index: Int
    ): CoinbaseParams

    override fun deserialize(decoder: Decoder): Coinbase =
        with(decoder.beginStructure(descriptor)) {
            lateinit var hash: Hash
            lateinit var payout: Payout
            lateinit var difficulty: Difficulty
            var blockheight by Delegates.notNull<Long>()
            var extraNonce by Delegates.notNull<Long>()
            lateinit var coinbaseParams: CoinbaseParams
            lateinit var witnesses: MutableSortedList<Witness>
            loop@ while (true) {
                @Suppress("UNCHECKED_CAST")
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> hash = decodeHash(i)
                    1 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    2 -> difficulty = decodeDifficulty(i)
                    3 -> blockheight = decodeLongElement(descriptor, i)
                    4 -> extraNonce = decodeLongElement(descriptor, i)
                    5 -> coinbaseParams = decodeCoinbaseParams(i)
                    6 -> witnesses = decodeSerializableElement(
                        descriptor, i, witnessesSerializer
                    ) as MutableSortedList<Witness>
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedCoinbaseImpl(
                CoinbaseImpl(
                    _witnesses = witnesses,
                    _payout = payout,
                    _difficulty = difficulty,
                    _blockheight = blockheight,
                    _extraNonce = extraNonce,
                    coinbaseParams = coinbaseParams
                ), hash
            )
        }


    override fun serialize(encoder: Encoder, value: Coinbase) {
        with(encoder.beginStructure(descriptor)) {
            encodeHash(0, value.hash)
            encodeSerializableElement(
                descriptor, 1, PayoutSerializer, value.payout
            )
            encodeDifficulty(2, value.difficulty)
            encodeLongElement(descriptor, 3, value.blockheight)
            encodeLongElement(descriptor, 4, value.extraNonce)
            encodeCoinbaseParams(5, value.coinbaseParams)
            encodeSerializableElement(
                descriptor, 6, witnessesSerializer,
                value.witnesses
            )
            endStructure(descriptor)
        }
    }
}