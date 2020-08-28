package org.knowledger.ledger.storage.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.compositeDecode
import java.util.*

/**
 * TODO: Switch back to manually sorted sets.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class SortedSetSerializer<T>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<SortedSet<T>> {
    override val descriptor: SerialDescriptor = TreeSetDescriptor(valueSerializer.descriptor)


    private fun patch(decoder: Decoder, old: TreeSet<T>): SortedSet<T> {
        val builder = old as? TreeSet<T> ?: TreeSet(old)
        val startIndex = builder.size

        return compositeDecode(decoder) {
            val size = readSize(this)
            //Can be read in entirety sequentially
            if (decodeSequentially()) {
                readAll(this, builder, startIndex, size)
            } else {
                while (true) {
                    //Read element by element
                    when (val index = decodeElementIndex(descriptor)) {
                        DECODE_DONE -> break
                        else -> readItem(this, startIndex + index, builder)
                    }
                }
            }
            builder
        }

    }

    override fun deserialize(decoder: Decoder): SortedSet<T> {
        val builder = TreeSet<T>()
        return patch(decoder, builder)
    }

    override fun serialize(encoder: Encoder, value: SortedSet<T>) {
        val size = value.size

        @Suppress("NAME_SHADOWING")
        val encoder = encoder.beginCollection(descriptor, size)
        val iterator = value.iterator()
        for (index in 0 until size)
            encoder.encodeSerializableElement(
                descriptor, index,
                valueSerializer, iterator.next()
            )
        encoder.endStructure(descriptor)
    }


    private fun readSize(decoder: CompositeDecoder): Int =
        decoder.decodeCollectionSize(descriptor)

    private fun readItem(decoder: CompositeDecoder, index: Int, builder: TreeSet<T>) {
        builder.add(decoder.decodeSerializableElement(descriptor, index, valueSerializer))
    }

    private fun readAll(
        decoder: CompositeDecoder, builder: TreeSet<T>, startIndex: Int, size: Int,
    ) {
        require(size >= 0) { "Size must be known in advance when using READ_ALL" }
        for (index in 0 until size) readItem(decoder, startIndex + index, builder)
    }

    class TreeSetDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
        override val serialName: String = "java.util.TreeSet"
        override val kind: SerialKind get() = StructureKind.LIST
        override val elementsCount: Int = 1
        override fun getElementName(index: Int): String = index.toString()
        override fun getElementIndex(name: String): Int =
            name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

        override fun getElementAnnotations(index: Int): List<Annotation> = elementDesc.annotations

        override fun getElementDescriptor(index: Int): SerialDescriptor = elementDesc

        override fun isElementOptional(index: Int): Boolean = false

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TreeSetDescriptor) return false

            if (elementDesc == other.elementDesc && serialName == other.serialName) return true

            return false
        }


        override fun hashCode(): Int {
            return elementDesc.hashCode() * 31 + serialName.hashCode()
        }

    }
}