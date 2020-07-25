package org.knowledger.ledger.database.adapters

import org.knowledger.ledger.database.StorageType

/**
 * Describes a schema for any type of value to be handled by
 * persisent storage. It's properties must abide by possible
 * types defined by [StorageType].
 */
interface SchemaProvider : Comparable<SchemaProvider> {
    val id: String
    val properties: Map<String, StorageType>

    override fun compareTo(other: SchemaProvider): Int =
        id.compareTo(other.id)
}