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
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl

@Serializer(forClass = HashedTransaction::class)
object HashedTransactionSerializer : KSerializer<HashedTransaction> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("HashedTransaction") {
            init {
                addElement("type")
                val descriptor = HashedTransactionImpl
                    .serializer()
                    .descriptor
                val elements = descriptor.elementsCount
                var i = 0
                while (i < elements) {
                    addElement(
                        descriptor.getElementName(i)
                    )
                    i++
                }
            }
        }

    override fun deserialize(decoder: Decoder): HashedTransaction =
        with(decoder.beginStructure(descriptor)) {
            lateinit var transaction: SignedTransactionImpl
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> decodeStringElement(descriptor, i)
                    1 -> transaction = decodeSerializableElement(
                        descriptor, 1, SignedTransactionImpl.serializer()
                    )
                    2 -> hash = decodeSerializableElement(
                        descriptor, 2, Hash.serializer()
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedTransactionImpl(
                transaction,
                hash
            )
        }

    override fun serialize(encoder: Encoder, obj: HashedTransaction) {
        with(encoder.beginStructure(descriptor)) {
            encodeStringElement(descriptor, 0, "HashedTransaction")
            encodeSerializableElement(
                descriptor, 1, SignedTransactionImpl.serializer(),
                (obj as HashedTransactionImpl).signedTransaction
            )
            encodeSerializableElement(
                descriptor, 2, Hash.serializer(), obj.hash
            )
            endStructure(descriptor)
        }
    }
}