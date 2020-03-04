package org.knowledger.ledger.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted

@Serializer(forClass = MutableSortedList::class)
class MutableSortedListSerializer<T : Comparable<T>>(
    valueSerializer: KSerializer<T>
) : KSerializer<MutableSortedList<T>> {
    private val delegate: KSerializer<List<T>> = ListSerializer(valueSerializer)

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): MutableSortedList<T> =
        delegate.deserialize(decoder).toMutableSortedListFromPreSorted()

    override fun serialize(encoder: Encoder, value: MutableSortedList<T>) {
        delegate.serialize(encoder, value)
    }

}