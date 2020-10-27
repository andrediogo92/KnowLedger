package org.knowledger.ledger.test

import org.knowledger.database.orient.OrientDatabase
import org.knowledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.chain.handles.LedgerHandle
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.results.unwrapFailure
import org.knowledger.testing.core.defaultHasher

class TestPublicApi {
    private val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    private val session = db.newManagedSession("test")
    private val hasher = defaultHasher
    private val temperatureDataStorageAdapter =
        TemperatureDataStorageAdapter(hasher)
    private val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrapFailure()
        .withCustomDB(db, session)
        .withHasher(hasher).build()
        .unwrapFailure()

    private val encoder = ledger.encoder
    private val identity = ledger.getIdentityByTag("test").unwrapFailure()
    private val temperatureChain: ChainHandle =
        ledger.registerNewChainHandleOf(temperatureDataStorageAdapter).unwrapFailure()
}