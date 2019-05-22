package pt.um.masb.common.database

interface ManagedSession : NewInstanceSession {
    val managedSchemas: ManagedSchemas

    fun reOpenIfNecessary()
    fun close()
    fun query(query: String, params: Map<String, Any>): StorageResults
    fun save(elem: StorageElement): StorageElement?
    fun save(elem: StorageElement, cluster: String): StorageElement?
    fun makeActive()
}