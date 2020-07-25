package org.knowledger.ledger.database.adapters

/**
 * Thrown in the face of an attempt to query for
 * a storage adapter for a type that has not been
 * registered in any ledger.
 */
class StorageAdapterNotRegistered(
    val clazz: String
) : Exception() {
    override val message: String?
        get() = "Unregistered storage adapter: $clazz"

    //Disregard stack trace. This exception is only thrown for physical data.
    override fun fillInStackTrace(): Throwable = this
}
