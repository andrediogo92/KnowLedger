package org.knowledger.ledger.storage.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.collections.SortedList
import org.knowledger.collections.toSortedListFromPreSorted

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SortedList::class)
class SortedListSerializer<T : Comparable<T>>(
    valueSerializer: KSerializer<T>
) : KSerializer<SortedList<T>> {
    private val delegate: KSerializer<List<T>> = ListSerializer(valueSerializer)

    override val descriptor: SerialDescriptor get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): SortedList<T> =
        delegate.deserialize(decoder).toSortedListFromPreSorted()

    override fun serialize(encoder: Encoder, value: SortedList<T>) {
        delegate.serialize(encoder, value)
    }

}