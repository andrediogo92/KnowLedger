package org.knowledger.ledger.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.util.*


/**
 * This class composes an adapter for any element type into an
 * adapter for a sorted set of those elements. For example, given
 * a {@code JsonAdapter<MovieTicket>}, use this to get a {@code
 * JsonAdapter<SortedSet<MovieTicket>>}. It works by looping over
 * the input elements when both reading and writing.
 */
class SortedSetJsonAdapter<T>(
    private val elementAdapter: JsonAdapter<T>
) : JsonAdapter<SortedSet<T>>() {
    override fun fromJson(reader: JsonReader): SortedSet<T> {
        val tree: TreeSet<T> = sortedSetOf()
        reader.beginArray()
        while (reader.hasNext()) {
            elementAdapter.fromJson(reader)?.let {
                tree += it
            }
        }
        reader.endArray()
        return tree
    }

    override fun toJson(writer: JsonWriter, value: SortedSet<T>?) {
        value?.let {
            writer.beginArray()
            value.forEach {
                elementAdapter.toJson(writer, it)
            }
            writer.endArray()
        }
    }


}