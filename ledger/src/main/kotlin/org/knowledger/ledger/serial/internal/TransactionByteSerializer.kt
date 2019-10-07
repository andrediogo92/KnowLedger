package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ByteArraySerializer
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PublicKeySerializer
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.TransactionSerializer
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import java.security.PublicKey

@Serializer(forClass = Transaction::class)
internal object TransactionByteSerializer : KSerializer<Transaction> {
    override val descriptor: SerialDescriptor =
        TransactionSerializer.descriptor

    override fun deserialize(decoder: Decoder): Transaction =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: PublicKey
            lateinit var data: PhysicalData
            lateinit var signature: ByteArray
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> publicKey = decodeSerializableElement(
                        descriptor, i, PublicKeySerializer
                    )
                    1 -> data = decodeSerializableElement(
                        descriptor, i, PhysicalData.serializer()
                    )
                    2 -> signature = decodeSerializableElement(
                        descriptor, i, ByteArraySerializer
                    )
                    3 -> hash = decodeSerializableElement(
                        descriptor, i, HashSerializer
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
            encodeSerializableElement(
                descriptor, 0, PublicKeySerializer, obj.publicKey
            )
            encodeSerializableElement(
                descriptor, 1, PhysicalData.serializer(),
                obj.data
            )
            encodeSerializableElement(
                descriptor, 2, ByteArraySerializer, obj.signature
            )
            encodeSerializableElement(
                descriptor, 3, HashSerializer, obj.hash
            )
            endStructure(descriptor)
        }
    }
}