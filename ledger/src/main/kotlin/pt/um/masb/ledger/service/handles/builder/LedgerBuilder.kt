package pt.um.masb.ledger.service.handles.builder

import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.handles.LedgerHandle
import java.io.File

interface LedgerBuilder<T> {
    fun withDBPath(path: File): Outcome<T, LedgerHandle.Failure>
    fun withDBPath(path: String): T
    fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): T

    fun withCustomDB(db: ManagedDatabase): T
    fun build(): Outcome<LedgerHandle, LedgerHandle.Failure>
}