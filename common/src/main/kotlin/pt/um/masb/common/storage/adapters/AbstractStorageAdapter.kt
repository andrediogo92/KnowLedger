package pt.um.masb.common.storage.adapters

import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.storage.results.DataResult

/**
 * An abstract storage adapter automatically provides a unique hashed
 * id to assign to a class' objects when stored in the database.
 *
 * The hashed id is based on the class name extracted via reflection.
 */
abstract class AbstractStorageAdapter<T : BlockChainData>(
    val clazz: Class<out T>
) : StorageAdapter<T> {
    override val id: String = base64Encode(
        clazz.name?.let {
            AvailableHashAlgorithms.SHA256Hasher.applyHash(it)
        } ?: throw NotBaseClassException()
    )

    protected inline fun <T : BlockChainData> commonLoad(
        document: StorageElement,
        tName: String,
        loader: StorageElement.() -> DataResult<T>
    ): DataResult<T> {
        return try {
            val name = document.schema
            if (name != null) {
                if (tName == name) {
                    loader(document)
                } else {
                    DataResult.UnexpectedClass(
                        "Got document with unexpected class: $name"
                    )
                }
            } else {
                DataResult.NonRegisteredSchema(
                    "Schema not existent for: ${document.print()}"
                )
            }
        } catch (e: Exception) {
            DataResult.QueryFailure(
                e.message ?: "", e
            )
        }
    }
}