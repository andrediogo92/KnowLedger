package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import java.security.PublicKey

internal abstract class AbstractTransactionSerializer : KSerializer<Transaction>,
                                                        HashEncode {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Transaction") {
            element(
                elementName = "publicKey",
                descriptor = publicKeyDescriptor
            )
            element(
                elementName = "signature",
                descriptor = signatureDescriptor
            )
            element(
                elementName = "hash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "data",
                descriptor = PhysicalData.serializer().descriptor
            )
        }

    abstract val publicKeyDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: PublicKey
    )

    abstract fun CompositeDecoder.decodePublicKey(index: Int): PublicKey

    abstract val signatureDescriptor: SerialDescriptor
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

    override fun serialize(encoder: Encoder, value: Transaction) {
        with(encoder.beginStructure(descriptor)) {
            encodePublicKey(0, value.publicKey)
            encodeSignature(1, value.signature)
            encodeHash(2, value.hash)
            encodeSerializableElement(
                descriptor, 3, PhysicalData.serializer(),
                value.data
            )
            endStructure(descriptor)
        }
    }
}