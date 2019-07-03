package pt.um.masb.common.database

interface TransactionableSession {
    fun begin(): ManagedSession
    fun commit(): ManagedSession
    fun rollback(): ManagedSession
}