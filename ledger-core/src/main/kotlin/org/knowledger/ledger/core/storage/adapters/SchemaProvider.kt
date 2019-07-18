package org.knowledger.ledger.core.storage.adapters

import org.knowledger.ledger.core.database.StorageType

/**
 * Describes a schema for any type of value to be handled by
 * persisent storage. It's properties must abide by possible
 * types defined by [StorageType].
 */
interface SchemaProvider<T> {
    val id: String
    val properties: Map<String, StorageType>
}