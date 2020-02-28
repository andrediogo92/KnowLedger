package org.knowledger.ledger.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.ArrayListSerializer
import org.knowledger.collections.SortedList

class SortedListSerializer<T : Comparable<T>>(
    valueSerializer: KSerializer<T>
) : KSerializer<SortedList<T>> {
    private val delegate: ArrayListSerializer<T> = ArrayListSerializer(valueSerializer)

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): SortedList<T> {
        return SortedList(delegate.deserialize(decoder) as MutableList<T>)
    }

    override fun serialize(encoder: Encoder, obj: SortedList<T>) {
        delegate.serialize(encoder, obj)
    }

}