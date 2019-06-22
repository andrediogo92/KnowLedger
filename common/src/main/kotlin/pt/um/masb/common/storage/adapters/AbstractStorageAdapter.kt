package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.misc.extractIdFromClass
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.DataFailure

/**
 * An abstract storage adapter automatically provides a unique hashed
 * id to assign to a class' objects when stored in the database.
 *
 * The hashed id is based on the class name extracted via reflection.
 */
abstract class AbstractStorageAdapter<T : BlockChainData>(
    val clazz: Class<out T>
) : StorageAdapter<T> {
    override val id: String by lazy {
        extractIdFromClass(clazz)
    }

    protected inline fun <T : BlockChainData> commonLoad(
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