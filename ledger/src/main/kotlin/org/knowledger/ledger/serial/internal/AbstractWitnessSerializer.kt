package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.HashedWitnessImpl
import org.knowledger.ledger.storage.witness.WitnessImpl
import kotlin.properties.Delegates

internal abstract class AbstractWitnessSerializer(
    transactionOutputSerializer: KSerializer<TransactionOutput>
) : KSerializer<Witness>, HashEncode {
    private object TransactionOutputSerialDescriptor : SerialClassDescImpl("TransactionOutput") {
        init {
            addElement("publicKey")
            addElement("previousWitnessIndex")
            addElement("previousCoinbase")
            addElement("hash")
            addElement("payout")
            addElement("transactionOutputs")
        }
    }

    override val descriptor: SerialDescriptor = TransactionOutputSerialDescriptor

    abstract fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: EncodedPublicKey
    )

    abstract fun CompositeDecoder.decodePublicKey(
        index: Int
    ): EncodedPublicKey

    private val transactionOutputsSerializer = SortedListSerializer(transactionOutputSerializer)

    override fun deserialize(decoder: Decoder): Witness =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: EncodedPublicKey
            var previousWitnessIndex: Int by Delegates.notNull()
            lateinit var previousCoinbase: Hash
            lateinit var hash: Hash
            lateinit var payout: Payout
            lateinit var transactionOutputs: MutableSortedList<TransactionOutput>
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> publicKey = decodePublicKey(i)
                    1 -> previousWitnessIndex = decodeIntElement(descriptor, i)
                    2 -> previousCoinbase = decodeHash(i)
                    3 -> hash = decodeHash(i)
                    4 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    5 -> transactionOutputs = decodeSerializableElement(
                        descriptor, i, transactionOutputsSerializer
                    ) as MutableSortedList<TransactionOutput>
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedWitnessImpl(
                WitnessImpl(
                    publicKey = publicKey,
                    previousWitnessIndex = previousWitnessIndex,
                    previousCoinbase = previousCoinbase,
                    _payout = payout,
                    _transactionOutputs = transactionOutputs
                ), hash
            )
        }

    override fun serialize(encoder: Encoder, obj: Witness) {
        with(encoder.beginStructure(descriptor)) {
            encodePublicKey(0, obj.publicKey)
            encodeIntElement(descriptor, 1, obj.previousWitnessIndex)
            encodeHash(2, obj.previousCoinbase)
            encodeHash(3, obj.hash)
            encodeSerializableElement(
                descriptor, 4, PayoutSerializer, obj.payout
            )
            encodeSerializableElement(
                descriptor, 5, transactionOutputsSerializer,
                obj.transactionOutputs
            )
            endStructure(descriptor)
        }
    }
}