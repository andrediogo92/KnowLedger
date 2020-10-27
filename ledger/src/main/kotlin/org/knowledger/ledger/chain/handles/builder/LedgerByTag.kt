package org.knowledger.ledger.chain.handles.builder

import com.github.michaelbull.result.map
import org.knowledger.ledger.chain.results.LedgerBuilderFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.LedgerId
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.config.ledger.ImmutableLedgerParams
import java.io.File

internal class LedgerByTag(
    val identity: String,
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByTag> {
    private var ledgerParams: LedgerParams? = null
    override lateinit var hash: Hash

    override fun withDBPath(path: File): Outcome<LedgerByTag, LedgerBuilderFailure> =
        setDBPath(path).map { this }

    override fun withDBPath(path: String): LedgerByTag =
        apply { this.path = path }

    fun withHasher(hasher: Hashers): LedgerByTag =
        apply { this.hashers = hasher }

    fun withCustomParams(ledgerParams: ImmutableLedgerParams): LedgerByTag =
        apply { this.ledgerParams = factories.ledgerParamsFactory.create(ledgerParams) }

    fun withFormula(formula: DataFormula): LedgerByTag =
        apply { this.formula = formula }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType, dbUser: String?, dbPassword: String?,
    ): LedgerByTag = apply {
        setCustomSession(dbOpenMode, dbSessionType, dbUser, dbPassword)
    }

    override fun withCustomDB(db: ManagedDatabase): LedgerByTag =
        apply { this.db = db }

    internal fun withCustomDB(db: ManagedDatabase, session: ManagedSession): LedgerByTag =
        apply { setCustomDB(db, session) }


    private fun generateLedgerParams(): LedgerParams {
        if (ledgerParams == null) {
            ledgerParams = factories.ledgerParamsFactory.create(hashers.id)
        }
        return ledgerParams as LedgerParams
    }

    override fun attemptToResolveId(): Outcome<LedgerId, LedgerBuilderFailure> {
        val ledgerParams = generateLedgerParams()
        val id = LedgerId(identity, hashers, encoder, ledgerParams)
        hash = id.hash
        return id.ok()
    }
}