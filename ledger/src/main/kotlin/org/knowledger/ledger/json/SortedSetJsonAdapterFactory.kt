package org.knowledger.ledger.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * Moshi asks this class to create JSON adapters. It only knows how to
 * create JSON adapters for `SortedSet` types, so it returns null for
 * all other requests. When it does get a request for a `SortedSet<X>`,
 * it asks Moshi for an adapter of the element type `X` and then
 * uses that to create an adapter for the set.
 */
object SortedSetJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type, annotations: Set<Annotation>, moshi: Moshi
    ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) {
            return null // Annotations? This factory doesn't apply.
        }

        if (type !is ParameterizedType) {
            return null // No type parameter? This factory doesn't apply.
        }

        if (type.rawType !== SortedSet::class.java) {
            return null // Not a sorted set? This factory doesn't apply.
        }

        val elementType = type.actualTypeArguments[0]
        val elementAdapter = moshi.adapter<Any>(elementType)

        return SortedSetJsonAdapter(elementAdapter).nullSafe()
    }
}