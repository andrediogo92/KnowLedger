package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl
import java.security.PublicKey

internal abstract class AbstractTransactionOutputSerializer(
    hashSerializer: KSerializer<Hash>
) : KSerializer<TransactionOutput>, HashEncode {
    private object TransactionOutputSerialDescriptor : SerialClassDescImpl("TransactionOutput") {
        init {
            addElement("hash")
            addElement("publicKey")
            addElement("previousCoinbase")
            addElement("payout")
            addElement("transactionHashes")
        }
    }

    override val descriptor: SerialDescriptor = TransactionOutputSerialDescriptor

    abstract fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: PublicKey
    )

    abstract fun CompositeDecoder.decodePublicKey(
        index: Int
    ): PublicKey

    private val hashsSerializer = hashSerializer.set

    override fun deserialize(decoder: Decoder): TransactionOutput =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: PublicKey
            lateinit var previousCoinbase: Hash
            lateinit var payout: Payout
            lateinit var transactionHashes: MutableSet<Hash>
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> hash = decodeHash(i)
                    1 -> publicKey = decodePublicKey(i)
                    2 -> previousCoinbase = decodeHash(i)
                    3 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    4 -> transactionHashes = decodeSerializableElement(
                        descriptor, i, hashsSerializer
                    ) as MutableSet<Hash>
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedTransactionOutputImpl(
                TransactionOutputImpl(
                    publicKey = publicKey,
                    previousCoinbase = previousCoinbase,
                    _payout = payout,
                    _transactionHashes = transactionHashes
                ), hash
            )
        }

    override fun serialize(encoder: Encoder, obj: TransactionOutput) {
        with(encoder.beginStructure(descriptor)) {
            encodeHash(0, obj.hash)
            encodePublicKey(1, obj.publicKey)
            encodeHash(2, obj.previousCoinbase)
            encodeSerializableElement(
                descriptor, 3, PayoutSerializer, obj.payout
            )
            encodeSerializableElement(
                descriptor, 4, hashsSerializer,
                obj.transactionHashes
            )
            endStructure(descriptor)
        }
    }
}