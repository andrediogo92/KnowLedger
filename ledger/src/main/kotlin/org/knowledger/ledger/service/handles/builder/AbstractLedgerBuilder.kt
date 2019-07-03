package org.knowledger.ledger.service.handles.builder

import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.data.DefaultDiff
import org.knowledger.common.database.DatabaseMode
import org.knowledger.common.database.DatabaseType
import org.knowledger.common.database.ManagedDatabase
import org.knowledger.common.database.ManagedSession
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.base64Encode
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.PersistenceWrapper
import java.io.File

abstract class AbstractLedgerBuilder {
    protected var db: ManagedDatabase? = null
    protected var session: ManagedSession? = null
    protected var path: String = "./db"
    protected var dbMode: DatabaseMode = DatabaseMode.EMBEDDED
    protected var dbType: DatabaseType = DatabaseType.LOCAL
    protected var dbUser: String = "admin"
    protected var dbPassword: String = "admin"

    internal lateinit var ledgerConfig: LedgerConfig
    internal lateinit var persistenceWrapper: PersistenceWrapper
    internal var hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER

    fun setDBPath(path: File): Outcome<Unit, LedgerHandle.Failure> =
        if (!path.exists() || path.isDirectory) {
            this.path = path.path
            Outcome.Ok(Unit)
        } else {
            Outcome.Error(
                LedgerHandle.Failure.PathCannotResolveAsDirectory(
                    """Path resolution error:
                            |   ${path.path} as directory: ${path.isDirectory}
                            |   ${path.path} exists: ${path.exists()}
                            """.trimMargin()
                )
            )
        }

    fun setCustomSession(
        dbOpenMode: DatabaseMode,
        dbSessionType: DatabaseType,
        dbUser: String?,
        dbPassword: String?
    ) {
        dbMode = dbOpenMode
        dbType = dbSessionType
        dbUser?.let {
            this.dbUser = it
        }
        dbPassword?.let {
            this.dbPassword = it
        }
    }

    internal fun setCustomDB(
        db: ManagedDatabase,
        session: ManagedSession
    ) {
        this.db = db
        this.session = session
    }

    protected fun addToContainers() {
        LedgerHandle.containers[base64Encode(ledgerConfig.ledgerId.hashId)] =
            LedgerContainer(
                ledgerConfig.ledgerId.hashId,
                hasher,
                ledgerConfig.ledgerParams,
                ledgerConfig.coinbaseParams,
                persistenceWrapper,
                DefaultDiff
            )
    }

}