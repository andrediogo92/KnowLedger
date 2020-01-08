package org.knowledger.ledger.storage.block

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun Block.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareBlock ->
            SABlockStorageAdapter.store(this, session)
        is BlockImpl ->
            SUBlockStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadBlockByImpl(
    ledgerHash: Hash
): Outcome<Block, LoadFailure> =
    SABlockStorageAdapter.load(ledgerHash, this)