package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
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
    override val descriptor: SerialDescriptor =
        SerialDescriptor("TransactionOutput") {
            val previousWitnessIndex = PrimitiveDescriptor(
                "previousWitnessIndex", PrimitiveKind.INT
            )
            element(
                elementName = "publicKey",
                descriptor = publicKeyDescriptor
            )
            element(
                elementName = previousWitnessIndex.serialName,
                descriptor = previousWitnessIndex
            )
            element(
                elementName = "previousCoinbase",
                descriptor = hashDescriptor
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
                elementName = "transactionOutputs",
                descriptor = transactionOutputSerializer.descriptor
            )
        }

    abstract val publicKeyDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: EncodedPublicKey
    )

    abstract fun CompositeDecoder.decodePublicKey(
        index: Int
    ): EncodedPublicKey

    private val transactionOutputsSerializer =
        SortedListSerializer(transactionOutputSerializer)

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
                    ).toMutableSortedListFromPreSorted()
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

    override fun serialize(encoder: Encoder, value: Witness) {
        with(encoder.beginStructure(descriptor)) {
            encodePublicKey(0, value.publicKey)
            encodeIntElement(descriptor, 1, value.previousWitnessIndex)
            encodeHash(2, value.previousCoinbase)
            encodeHash(3, value.hash)
            encodeSerializableElement(
                descriptor, 4, PayoutSerializer, value.payout
            )
            encodeSerializableElement(
                descriptor, 5, transactionOutputsSerializer,
                value.transactionOutputs
            )
            endStructure(descriptor)
        }
    }
}