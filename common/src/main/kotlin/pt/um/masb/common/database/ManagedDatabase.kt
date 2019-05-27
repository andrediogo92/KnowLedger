package pt.um.masb.common.database

interface ManagedDatabase {
    fun newManagedSession(dbName: String): ManagedSession
    fun close()
}