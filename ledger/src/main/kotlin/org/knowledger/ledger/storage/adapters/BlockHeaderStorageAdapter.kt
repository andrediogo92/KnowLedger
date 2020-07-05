package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.block.header.SUBlockHeaderStorageAdapter
import org.knowledger.ledger.storage.block.header.StorageAwareBlockHeader
import org.knowledger.ledger.storage.block.header.factory.StorageAwareBlockHeaderFactory

internal class BlockHeaderStorageAdapter(
    saBlockHeaderFactory: StorageAwareBlockHeaderFactory,
    chainIdStorageAdapter: ChainIdStorageAdapter
) : LedgerStorageAdapter<MutableBlockHeader> {
    private val suBlockHeaderStorageAdapter =
        SUBlockHeaderStorageAdapter(
            saBlockHeaderFactory, chainIdStorageAdapter
        )

    override val id: String
        get() = suBlockHeaderStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suBlockHeaderStorageAdapter.properties


    override fun store(
        toStore: MutableBlockHeader, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlockHeader -> session.cacheStore(
                suBlockHeaderStorageAdapter,
                toStore, toStore.blockHeader
            )
            else -> suBlockHeaderStorageAdapter.store(
                toStore, session
            )
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlockHeader, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suBlockHeaderStorageAdapter
        )

}