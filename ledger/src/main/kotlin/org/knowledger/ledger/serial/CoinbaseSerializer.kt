package org.knowledger.ledger.serial

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.base.hash.hashFromHexString
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import kotlin.properties.Delegates

@Serializer(forClass = Coinbase::class)
object CoinbaseSerializer : KSerializer<Coinbase> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("Coinbase") {
            init {
                addElement("transactionOutputs")
                addElement("payout")
                addElement("difficulty")
                addElement("blockheight")
                addElement("extraNonce")
                addElement("coinbaseParams")
                addElement("hash")
            }
        }

    val transactionOutputsSerializer = TransactionOutputSerializer.set


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
                        descriptor, i, CoinbaseParamsSerializer
                    )
                    6 -> hash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
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
                descriptor, 5, CoinbaseParamsSerializer, obj.coinbaseParams
            )
            encodeStringElement(
                descriptor, 6, obj.hash.toHexString()
            )
            endStructure(descriptor)
        }
    }
}