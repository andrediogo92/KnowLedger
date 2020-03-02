package org.knowledger.ledger.service.handles.builder

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

class LedgerByTag(
    val identity: String
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByTag> {
    private var ledgerParams: LedgerParams? = null
    private var coinbaseParams: CoinbaseParams? = null
    override lateinit var hash: Hash

    override fun withTypeStorageAdapters(
        types: Iterable<AbstractStorageAdapter<out LedgerData>>
    ): LedgerByTag =
        apply {
            registerAdapters(types)
        }

    override fun withDBPath(
        path: File
    ): Outcome<LedgerByTag, LedgerHandle.Failure> =
        setDBPath(path).mapSuccess {
            this
        }

    override fun withDBPath(path: String): LedgerByTag =
        apply {
            this.path = path
        }

    fun withHasher(hasher: Hashers): LedgerByTag =
        apply {
            this.hasher = hasher
        }

    fun withCustomParams(
        ledgerParams: LedgerParams
    ): LedgerByTag =
        apply {
            this.ledgerParams = ledgerParams
        }

    fun withCoinbaseParams(
        formula: DataFormula,
        coinbaseParams: CoinbaseParams
    ): LedgerByTag =
        apply {
            LedgerHandle.registerFormula(formula)
            this.coinbaseParams = coinbaseParams
        }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): LedgerByTag = apply {
        setCustomSession(
            dbOpenMode, dbSessionType,
            dbUser, dbPassword
        )
    }

    override fun withCustomDB(db: ManagedDatabase): LedgerByTag =
        apply {
            this.db = db
        }

    internal fun withCustomDB(
        db: ManagedDatabase, session: ManagedSession
    ) =
        apply {
            setCustomDB(db, session)
        }


    private fun generateLedgerParams() {
        if (ledgerParams == null) {
            ledgerParams = LedgerParams(hasher.id)
        }
        if (coinbaseParams == null) {
            coinbaseParams = CoinbaseParams()
        }
    }

    override fun attemptToResolveId(): Outcome<LedgerConfig, LedgerHandle.Failure> {
        generateLedgerParams()
        val config = LedgerConfig(
            LedgerId(identity, hasher, encoder),
            ledgerParams!!, coinbaseParams!!
        )
        hash = config.ledgerId.hash
        return Outcome.Ok(config)
    }
}