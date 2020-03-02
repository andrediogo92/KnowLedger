package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.SAChainIdStorageAdapter
import org.knowledger.ledger.config.chainid.SUChainIdStorageAdapter
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object ChainIdStorageAdapter : ServiceStorageAdapter<ChainId>,
                                        SchemaProvider by SUChainIdStorageAdapter {
    override fun store(
        toStore: ChainId, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareChainId ->
                SAChainIdStorageAdapter.store(toStore, session)
            is ChainIdImpl ->
                SUChainIdStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainId, LedgerFailure> =
        SAChainIdStorageAdapter.load(ledgerHash, element)
}