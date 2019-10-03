package org.knowledger.ledger.serial

import kotlinx.serialization.*
import java.util.*

/**
 * TODO: Switch back to manually sorted sets.
 */
@Serializer(forClass = SortedSet::class)
class SortedSetSerializer<T>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<SortedSet<T>> {
    override val descriptor = TreeSetDescriptor(valueSerializer.descriptor)


    fun patch(decoder: Decoder, old: TreeSet<T>): SortedSet<T> {
        val builder = old as? TreeSet<T> ?: TreeSet(old)
        val startIndex = builder.size
        @Suppress("NAME_SHADOWING")
        val decoder = decoder.beginStructure(descriptor, valueSerializer)
        val size = readSize(decoder)
        mainLoop@ while (true) {
            val index = decoder.decodeElementIndex(descriptor)
            when (index) {
                CompositeDecoder.READ_ALL -> {
                    readAll(decoder, builder, startIndex, size)
                    break@mainLoop
                }
                CompositeDecoder.READ_DONE -> break@mainLoop
                else -> readItem(decoder, startIndex + index, builder)
            }

        }
        decoder.endStructure(descriptor)
        return builder
    }

    override fun deserialize(decoder: Decoder): SortedSet<T> {
        val builder = TreeSet<T>()
        return patch(decoder, builder)
    }

    override fun serialize(encoder: Encoder, obj: SortedSet<T>) {
        val size = obj.size
        @Suppress("NAME_SHADOWING")
        val encoder = encoder.beginCollection(descriptor, size, valueSerializer)
        val iterator = obj.iterator()
        for (index in 0 until size)
            encoder.encodeSerializableElement(descriptor, index, valueSerializer, iterator.next())
        encoder.endStructure(descriptor)
    }


    private fun readSize(decoder: CompositeDecoder): Int =
        decoder.decodeCollectionSize(descriptor)

    private fun readItem(
        decoder: CompositeDecoder, index: Int,
        builder: TreeSet<T>
    ) {
        builder.add(
            decoder.decodeSerializableElement(
                descriptor, index, valueSerializer
            )
        )
    }

    private fun readAll(
        decoder: CompositeDecoder, builder: TreeSet<T>,
        startIndex: Int, size: Int
    ) {
        require(size >= 0) { "Size must be known in advance when using READ_ALL" }
        for (index in 0 until size)
            readItem(decoder, startIndex + index, builder)
    }

    class TreeSetDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
        override val name: String = "java.util.TreeSet"
        override val kind: SerialKind get() = StructureKind.LIST
        override val elementsCount: Int = 1
        override fun getElementName(index: Int): String = index.toString()
        override fun getElementIndex(name: String): Int =
            name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

        override fun getElementDescriptor(index: Int): SerialDescriptor = elementDesc

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TreeSetDescriptor) return false

            if (elementDesc == other.elementDesc && name == other.name) return true

            return false
        }

        override fun hashCode(): Int {
            return elementDesc.hashCode() * 31 + name.hashCode()
        }
    }
}