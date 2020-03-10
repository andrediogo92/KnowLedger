package org.knowledger.ledger.test

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.orient.OrientDatabase
import org.knowledger.ledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.testing.ledger.testHasher

class TestPublicApi {
    private val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    private val session = db.newManagedSession("test")
    private val hasher = testHasher
    private val temperatureDataStorageAdapter =
        TemperatureDataStorageAdapter(hasher)
    private val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrap()
        .withCustomDB(db, session)
        .withHasher(hasher)
        .withCustomParams(
            LedgerParams(
                hasher.id,
                blockParams = BlockParams(
                    blockLength = 20,
                    blockMemorySize = 500000
                )
            )
        ).withTypeStorageAdapters(
            temperatureDataStorageAdapter
        ).build()
        .unwrap()

    private val encoder = ledger.encoder
    private val identity = ledger.getIdentityByTag("test").unwrap()
    private val temperatureChain: ChainHandle =
        ledger.registerNewChainHandleOf(
            temperatureDataStorageAdapter
        ).unwrap()
}