package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LedgerFailure

internal fun ChainId.store(session: ManagedSession): StorageElement =
    when (this) {
        is StorageAwareChainId ->
            SAChainIdStorageAdapter.store(this, session)
        is StorageUnawareChainId ->
            SUChainIdStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadChainIdByImpl(
    ledgerHash: Hash
): Outcome<ChainId, LedgerFailure> =
    SAChainIdStorageAdapter.load(ledgerHash, this)