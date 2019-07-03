package org.knowledger.ledger.service.handles.builder

import org.knowledger.common.database.DatabaseMode
import org.knowledger.common.database.DatabaseType
import org.knowledger.common.database.ManagedDatabase
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.handles.LedgerHandle
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