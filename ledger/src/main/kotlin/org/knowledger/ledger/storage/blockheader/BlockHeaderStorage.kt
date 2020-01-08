package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun HashedBlockHeader.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareBlockHeader ->
            SABlockHeaderStorageAdapter.store(this, session)
        is HashedBlockHeaderImpl ->
            SUBlockHeaderStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadBlockHeaderByImpl(
    ledgerHash: Hash
): Outcome<HashedBlockHeader, LoadFailure> =
    SABlockHeaderStorageAdapter.load(ledgerHash, this)