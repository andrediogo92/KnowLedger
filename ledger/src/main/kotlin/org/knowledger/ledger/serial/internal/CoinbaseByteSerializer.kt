package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.CoinbaseSerializer
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import kotlin.properties.Delegates

@Serializer(forClass = Coinbase::class)
object CoinbaseByteSerializer : KSerializer<Coinbase> {
    override val descriptor: SerialDescriptor =
        CoinbaseSerializer.descriptor

    val transactionOutputsSerializer = TransactionOutputByteSerializer.set

    override fun deserialize(decoder: Decoder): Coinbase =
        with(decoder.beginStructure(descriptor)) {
            lateinit var transactionOutputs: MutableSet<TransactionOutput>
            lateinit var payout: Payout
            lateinit var difficulty: Difficulty
            var blockheight by Delegates.notNull<Long>()
            var extraNonce by Delegates.notNull<Long>()
            lateinit var coinbaseParams: CoinbaseParams
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> transactionOutputs = decodeSerializableElement(
                        descriptor, i, transactionOutputsSerializer
                    ) as MutableSet<TransactionOutput>
                    1 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    2 -> difficulty = decodeSerializableElement(
                        descriptor, i, DifficultySerializer
                    )
                    3 -> blockheight = decodeLongElement(
                        descriptor, i
                    )
                    4 -> extraNonce = decodeLongElement(
                        descriptor, i
                    )
                    5 -> coinbaseParams = decodeSerializableElement(
                        descriptor, i, CoinbaseParamsByteSerializer
                    )
                    6 -> hash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedCoinbaseImpl(
                CoinbaseImpl(
                    _transactionOutputs = transactionOutputs,
                    payout = payout,
                    difficulty = difficulty,
                    blockheight = blockheight,
                    extraNonce = extraNonce,
                    coinbaseParams = coinbaseParams
                ), hash
            )
        }

    override fun serialize(encoder: Encoder, obj: Coinbase) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, transactionOutputsSerializer,
                obj.transactionOutputs
            )
            encodeSerializableElement(
                descriptor, 1, PayoutSerializer, obj.payout
            )
            encodeSerializableElement(
                descriptor, 2, DifficultySerializer, obj.difficulty
            )
            encodeLongElement(
                descriptor, 3, obj.blockheight
            )
            encodeLongElement(
                descriptor, 4, obj.extraNonce
            )
            encodeSerializableElement(
                descriptor, 5, CoinbaseParamsByteSerializer, obj.coinbaseParams
            )
            encodeSerializableElement(
                descriptor, 6, HashSerializer, obj.hash
            )
            endStructure(descriptor)
        }
    }
}