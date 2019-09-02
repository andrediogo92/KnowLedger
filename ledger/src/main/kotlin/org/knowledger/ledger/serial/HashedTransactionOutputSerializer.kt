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
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl

@Serializer(forClass = HashedTransactionOutput::class)
object HashedTransactionOutputSerializer : KSerializer<HashedTransactionOutput> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("HashedTransactionOutput") {
            init {
                addElement("type")
                val descriptor = HashedTransactionOutputImpl
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

    override fun deserialize(decoder: Decoder): HashedTransactionOutput =
        with(decoder.beginStructure(descriptor)) {
            lateinit var transactionOutput: TransactionOutputImpl
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> decodeStringElement(descriptor, i)
                    1 -> transactionOutput = decodeSerializableElement(
                        descriptor, 1, TransactionOutputImpl.serializer()
                    )
                    2 -> hash = decodeSerializableElement(
                        descriptor, 2, Hash.serializer()
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedTransactionOutputImpl(
                transactionOutput,
                hash
            )
        }

    override fun serialize(encoder: Encoder, obj: HashedTransactionOutput) {
        with(encoder.beginStructure(descriptor)) {
            encodeStringElement(descriptor, 0, "HashedTransactionOutput")
            encodeSerializableElement(
                descriptor, 1, TransactionOutputImpl.serializer(),
                (obj as HashedTransactionOutputImpl).transactionOutput
            )
            encodeSerializableElement(
                descriptor, 2, Hash.serializer(), obj.hash
            )
            endStructure(descriptor)
        }
    }
}