package org.knowledger.ledger.chain.handles.builder

import org.knowledger.ledger.builders.Builder
import org.knowledger.ledger.chain.handles.LedgerHandle
import org.knowledger.ledger.chain.results.LedgerBuilderFailure
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.results.Outcome
import java.io.File

internal interface LedgerBuilder<T> : Builder<LedgerHandle, LedgerBuilderFailure> {
    fun withDBPath(path: File): Outcome<T, LedgerBuilderFailure>
    fun withDBPath(path: String): T

    fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType, dbUser: String?, dbPassword: String?,
    ): T

    fun withCustomDB(db: ManagedDatabase): T
}