package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import kotlin.properties.Delegates

internal abstract class AbstractCoinbaseSerializer(
    transactionOutputSerializer: KSerializer<TransactionOutput>
) : KSerializer<Coinbase>, HashEncode {
    private object CoinbaseSerialDescriptor : SerialClassDescImpl("Coinbase") {
        init {
            addElement("hash")
            addElement("payout")
            addElement("difficulty")
            addElement("blockheight")
            addElement("extraNonce")
            addElement("coinbaseParams")
            addElement("transactionOutputs")
        }
    }

    override val descriptor: SerialDescriptor = CoinbaseSerialDescriptor

    private val transactionOutputsSerializer = transactionOutputSerializer.set

    abstract fun CompositeEncoder.encodeDifficulty(
        index: Int, difficulty: Difficulty
    )

    abstract fun CompositeDecoder.decodeDifficulty(
        index: Int
    ): Difficulty

    abstract fun CompositeEncoder.encodeCoinbaseParams(
        index: Int, params: CoinbaseParams
    )

    abstract fun CompositeDecoder.decodeCoinbaseParams(
        index: Int
    ): CoinbaseParams

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
                    0 -> hash = decodeHash(i)
                    1 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    2 -> difficulty = decodeDifficulty(i)
                    3 -> blockheight = decodeLongElement(descriptor, i)
                    4 -> extraNonce = decodeLongElement(descriptor, i)
                    5 -> coinbaseParams = decodeCoinbaseParams(i)
                    6 -> transactionOutputs = decodeSerializableElement(
                        descriptor, i, transactionOutputsSerializer
                    ) as MutableSet<TransactionOutput>
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedCoinbaseImpl(
                CoinbaseImpl(
                    _transactionOutputs = transactionOutputs,
                    _payout = payout,
                    _difficulty = difficulty,
                    _blockheight = blockheight,
                    _extraNonce = extraNonce,
                    coinbaseParams = coinbaseParams
                ), hash
            )
        }


    override fun serialize(encoder: Encoder, obj: Coinbase) {
        with(encoder.beginStructure(descriptor)) {
            encodeHash(0, obj.hash)
            encodeSerializableElement(
                descriptor, 1, PayoutSerializer, obj.payout
            )
            encodeDifficulty(2, obj.difficulty)
            encodeLongElement(descriptor, 3, obj.blockheight)
            encodeLongElement(descriptor, 4, obj.extraNonce)
            encodeCoinbaseParams(5, obj.coinbaseParams)
            encodeSerializableElement(
                descriptor, 6, transactionOutputsSerializer,
                obj.transactionOutputs
            )
            endStructure(descriptor)
        }
    }
}