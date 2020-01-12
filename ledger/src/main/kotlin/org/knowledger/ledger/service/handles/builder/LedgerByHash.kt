package org.knowledger.ledger.service.handles.builder

import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

data class LedgerByHash(
    internal var hash: Hash
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByHash> {
    inline fun withLedgerSerializationModule(
        crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
    ): LedgerByHash =
        apply {
            iSerialModule = iSerialModule.withLedger(with)
        }


    override fun withDBPath(
        path: File
    ): Outcome<LedgerByHash, LedgerHandle.Failure> =
        setDBPath(path).mapSuccess {
            this
        }

    override fun withDBPath(path: String): LedgerByHash =
        apply {
            this.path = path
        }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): LedgerByHash = apply {
        setCustomSession(
            dbOpenMode, dbSessionType,
            dbUser, dbPassword
        )
    }

    override fun withCustomDB(db: ManagedDatabase): LedgerByHash =
        apply {
            this.db = db
        }

    internal fun withCustomDB(
        db: ManagedDatabase, session: ManagedSession
    ) =
        apply {
            setCustomDB(db, session)
        }

    internal fun getLedgerHandleByHash(
        hash: Hash
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        session!!.query(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${LedgerConfigStorageAdapter.id}
                    WHERE hashId = :hashId
                """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes
                )
            )
        ).let {
            if (it.hasNext()) {
                LedgerConfigStorageAdapter.load(
                    hash, it.next().element
                )
            } else {
                Outcome.Error(
                    LedgerHandle.Failure.NonExistentLedger
                )
            }
        }


    private fun attemptToResolveId(): Outcome<LedgerConfig, LedgerHandle.Failure> =
        //Get ledger params
        getLedgerHandleByHash(hash).mapSuccess {
            hasher = Hashers.getHasher(it.ledgerParams.crypter)
            it
        }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        encoder = Cbor(UpdateMode.UPDATE, true, serialModule)
        buildDB(hash)
        return attemptToResolveId().mapSuccess {
            ledgerConfig = it
            ledgerInfo = LedgerInfo(
                ledgerId = ledgerConfig.ledgerId,
                hasher = hasher,
                ledgerParams = ledgerConfig.ledgerParams,
                coinbaseParams = ledgerConfig.coinbaseParams,
                serialModule = serialModule,
                persistenceWrapper = persistenceWrapper,
                formula = DefaultDiff,
                encoder = encoder
            )
            persistenceWrapper.initializeAdapters(ledgerInfo)
            persistenceWrapper.registerDefaultSchemas()
            LedgerHandle(this)
        }
    }
}