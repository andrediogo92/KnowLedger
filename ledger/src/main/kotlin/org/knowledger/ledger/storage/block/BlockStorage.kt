package org.knowledger.ledger.storage.block

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
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