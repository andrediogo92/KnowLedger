package pt.um.masb.common.database

interface ManagedDatabase {
    fun newManagedSession(): ManagedSession
    fun close()
}