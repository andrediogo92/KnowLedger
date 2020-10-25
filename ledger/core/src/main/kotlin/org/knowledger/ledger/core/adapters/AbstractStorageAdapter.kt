package org.knowledger.ledger.core.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.digest.classDigest
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import kotlin.reflect.KClass

/**
 * An abstract storage adapter automatically provides a unique hashed
 * id to assign to a class' objects when stored in the database.
 *
 * The hashed id is based on the class name extracted via reflection.
 */
abstract class AbstractStorageAdapter<T : LedgerData>(
    val clazz: KClass<out T>, hashers: Hashers,
) : StorageAdapter<T> {
    override val hash: Hash = clazz.classDigest(hashers)
    abstract val serializer: KSerializer<T>

    protected inline fun <T : LedgerData> commonLoad(
        document: StorageElement, tName: String,
        loader: StorageElement.() -> Outcome<T, DataFailure>,
    ): Outcome<T, DataFailure> = tryOrDataUnknownFailure {
        val name = document.schema
        if (name != null) {
            if (tName == name) {
                loader(document)
            } else {
                DataFailure.UnexpectedClass("Got document with unexpected class: $name").err()
            }
        } else {
            DataFailure.NonRegisteredSchema("Schema not existent for: ${document.json}").err()
        }
    }
}