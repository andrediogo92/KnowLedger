package org.knowledger.ledger.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.bytesFromHexString
import org.knowledger.ledger.core.misc.hashFromHexString
import org.knowledger.ledger.core.misc.toHexString
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import java.security.PublicKey

@Serializer(forClass = Transaction::class)
object TransactionSerializer : KSerializer<Transaction> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("Transaction") {
            init {
                addElement("publicKey")
                addElement("data")
                addElement("signature")
                addElement("hash")
            }
        }

    override fun deserialize(decoder: Decoder): Transaction =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: PublicKey
            lateinit var data: PhysicalData
            lateinit var signature: ByteArray
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> publicKey = decodeStringElement(
                        descriptor, i
                    ).bytesFromHexString().toPublicKey()
                    1 -> data = decodeSerializableElement(
                        descriptor, i, PhysicalData.serializer()
                    )
                    2 -> signature = decodeStringElement(
                        descriptor, i
                    ).bytesFromHexString()
                    3 -> hash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
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
            encodeStringElement(
                descriptor, 0, obj.publicKey.toHexString()
            )
            encodeSerializableElement(
                descriptor, 1, PhysicalData.serializer(),
                obj.data
            )
            encodeStringElement(
                descriptor, 2, obj.signature.toHexString()
            )
            encodeStringElement(
                descriptor, 3, obj.hash.print
            )
            endStructure(descriptor)
        }
    }
}