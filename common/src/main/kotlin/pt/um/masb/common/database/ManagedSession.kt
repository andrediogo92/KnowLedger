package pt.um.masb.common.database

interface ManagedSession : NewInstanceSession {
    val isClosed: Boolean
    val managedSchemas: ManagedSchemas

    fun reOpenIfNecessary()
    fun close()
    fun query(query: String, params: Map<String, Any>): StorageResults
    fun query(query: String): StorageResults
    fun save(elem: StorageElement): StorageElement?
    fun save(elem: StorageElement, cluster: String): StorageElement?
    fun makeActive()
}