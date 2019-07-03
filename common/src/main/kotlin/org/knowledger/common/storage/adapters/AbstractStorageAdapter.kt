package org.knowledger.common.storage.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.StorageElement
import org.knowledger.common.misc.extractIdFromClass
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.results.DataFailure

/**
 * An abstract storage adapter automatically provides a unique hashed
 * id to assign to a class' objects when stored in the database.
 *
 * The hashed id is based on the class name extracted via reflection.
 */
abstract class AbstractStorageAdapter<T : LedgerData>(
    val clazz: Class<out T>
) : StorageAdapter<T> {
    override val id: String by lazy {
        clazz.extractIdFromClass()
    }

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
                        "Schema not existent for: ${document.print()}"
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