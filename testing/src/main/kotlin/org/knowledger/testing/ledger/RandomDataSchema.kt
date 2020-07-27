package org.knowledger.testing.ledger

import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.adapters.SchemaProvider

class RandomDataSchema : SchemaProvider {
    override val id: String
        get() = "RandomData"
    override val properties: Map<String, StorageType>
        get() = emptyMap()
}