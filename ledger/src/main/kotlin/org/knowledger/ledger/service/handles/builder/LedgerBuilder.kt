package org.knowledger.ledger.service.handles.builder

import org.knowledger.ledger.builders.Builder
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

internal interface LedgerBuilder<T> : Builder<LedgerHandle, LedgerHandle.Failure> {
    fun withDBPath(path: File): Outcome<T, LedgerHandle.Failure>
    fun withDBPath(path: String): T
    fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): T

    fun withCustomDB(db: ManagedDatabase): T
}