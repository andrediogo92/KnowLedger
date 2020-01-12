package org.knowledger.ledger.service.handles.builder

import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

class LedgerByTag(
    val identity: String
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByTag> {
    private var ledgerParams: LedgerParams? = null
    private var coinbaseParams: CoinbaseParams? = null

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

    inline fun withLedgerSerializationModule(
        crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
    ): LedgerByTag =
        apply {
            iSerialModule = iSerialModule.withLedger(with)
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

    private fun attemptToResolveId() {
        ledgerConfig = LedgerConfig(
            LedgerId(identity, hasher, encoder), ledgerParams!!,
            coinbaseParams!!
        )
    }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        encoder = Cbor(UpdateMode.UPDATE, true, serialModule)
        generateLedgerParams()
        attemptToResolveId()
        buildDB(ledgerConfig.ledgerId.hash)
        ledgerInfo = LedgerInfo(
            ledgerId = ledgerConfig.ledgerId,
            hasher = hasher,
            ledgerParams = ledgerConfig.ledgerParams,
            coinbaseParams = ledgerConfig.coinbaseParams,
            serialModule = serialModule,
            formula = DefaultDiff,
            encoder = encoder,
            persistenceWrapper = persistenceWrapper
        )
        persistenceWrapper.initializeAdapters(ledgerInfo)
        persistenceWrapper.registerDefaultSchemas()
        return Outcome.Ok(LedgerHandle(this))
    }

}