package org.knowledger.testing.ledger

import org.knowledger.ledger.core.adapters.HashSchemaProvider
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.digest.classDigest
import org.knowledger.ledger.database.StorageType
import org.knowledger.testing.core.defaultHasher

class RandomDataSchema : HashSchemaProvider {
    override val hash: Hash
        get() = classDigest(defaultHasher)

    override val properties: Map<String, StorageType>
        get() = emptyMap()
}