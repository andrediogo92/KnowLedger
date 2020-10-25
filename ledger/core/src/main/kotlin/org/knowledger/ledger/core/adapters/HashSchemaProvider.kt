package org.knowledger.ledger.core.adapters

import org.knowledger.ledger.core.toTag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.adapters.SchemaProvider

interface HashSchemaProvider : SchemaProvider {
    val hash: Hash
    val tag: Tag get() = hash.toTag()
    override val id: String get() = tag.id
}