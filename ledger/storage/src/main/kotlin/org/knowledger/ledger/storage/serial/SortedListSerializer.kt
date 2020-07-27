package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import org.knowledger.collections.SortedList
import org.knowledger.collections.toSortedListFromPreSorted

@Serializer(forClass = SortedList::class)
class SortedListSerializer<T : Comparable<T>>(
    valueSerializer: KSerializer<T>
) : KSerializer<SortedList<T>> {
    private val delegate: KSerializer<List<T>> =
        ListSerializer(valueSerializer)

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): SortedList<T> =
        delegate.deserialize(decoder).toSortedListFromPreSorted()

    override fun serialize(encoder: Encoder, value: SortedList<T>) {
        delegate.serialize(encoder, value)
    }

}