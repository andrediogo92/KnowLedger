package org.knowledger.ledger.core.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.core.base.hash.classDigest
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome

/**
 * An abstract storage adapter automatically provides a unique hashed
 * id to assign to a class' objects when stored in the database.
 *
 * The hashed id is based on the class name extracted via reflection.
 */
abstract class AbstractStorageAdapter<T : LedgerData>(
    val clazz: Class<out T>,
    hasher: Hashers
) : StorageAdapter<T> {
    override val id: String =
        clazz.classDigest(hasher).base64Encoded()

    abstract val serializer: KSerializer<T>

    protected inline fun <T : LedgerData> commonLoad(
        document: StorageElement,
        tName: String,
        loader: StorageElement.() -> Outcome<T, DataFailure>
    ): Outcome<T, DataFailure> {
        return try {
            val name = document.schema
            if (name != null) {
                if (tName == name) {
                    loader(document)
                } else {
                    Outcome.Error<DataFailure>(
                        DataFailure.UnexpectedClass(
                            "Got document with unexpected class: $name"
                        )
                    )
                }
            } else {
                Outcome.Error(
                    DataFailure.NonRegisteredSchema(
                        "Schema not existent for: ${document.json}"
                    )
                )
            }
        } catch (e: Exception) {
            Outcome.Error(
                DataFailure.UnknownFailure(
                    e.message ?: "", e
                )
            )
        }
    }
}