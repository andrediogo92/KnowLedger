package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import java.security.PublicKey

internal abstract class AbstractTransactionSerializer : KSerializer<Transaction>, HashEncode {
    private object TransactionSerialDescriptor : SerialClassDescImpl("Transaction") {
        init {
            addElement("publicKey")
            addElement("signature")
            addElement("hash")
            addElement("data")
        }
    }

    override val descriptor: SerialDescriptor = TransactionSerialDescriptor

    abstract fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: PublicKey
    )

    abstract fun CompositeDecoder.decodePublicKey(index: Int): PublicKey

    abstract fun CompositeEncoder.encodeSignature(
        index: Int, encodedSignature: EncodedSignature
    )

    abstract fun CompositeDecoder.decodeSignature(index: Int): EncodedSignature

    override fun deserialize(decoder: Decoder): Transaction =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: PublicKey
            lateinit var data: PhysicalData
            lateinit var signature: EncodedSignature
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> publicKey = decodePublicKey(i)
                    1 -> signature = decodeSignature(i)
                    2 -> hash = decodeHash(i)
                    3 -> data = decodeSerializableElement(
                        descriptor, i, PhysicalData.serializer()
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedTransactionImpl(
                SignedTransactionImpl(
                    signature = signature,
                    publicKey = publicKey,
                    data = data
                ),
                hash
            )
        }

    override fun serialize(encoder: Encoder, obj: Transaction) {
        with(encoder.beginStructure(descriptor)) {
            encodePublicKey(0, obj.publicKey)
            encodeSignature(1, obj.signature)
            encodeHash(2, obj.hash)
            encodeSerializableElement(
                descriptor, 3, PhysicalData.serializer(),
                obj.data
            )
            endStructure(descriptor)
        }
    }
}