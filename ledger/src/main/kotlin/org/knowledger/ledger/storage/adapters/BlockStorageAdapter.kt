package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.block.SUBlockStorageAdapter
import org.knowledger.ledger.storage.block.StorageAwareBlock
import org.knowledger.ledger.storage.block.factory.StorageAwareBlockFactory

internal class BlockStorageAdapter(
    blockFactory: StorageAwareBlockFactory,
    transactionStorageAdapter: TransactionStorageAdapter,
    coinbaseStorageAdapter: CoinbaseStorageAdapter,
    blockHeaderStorageAdapter: BlockHeaderStorageAdapter,
    merkleTreeStorageAdapter: MerkleTreeStorageAdapter
) : LedgerStorageAdapter<MutableBlock> {
    private val suBlockStorageAdapter: SUBlockStorageAdapter =
        SUBlockStorageAdapter(
            blockFactory, transactionStorageAdapter,
            coinbaseStorageAdapter,
            blockHeaderStorageAdapter,
            merkleTreeStorageAdapter
        )

    override val id: String
        get() = suBlockStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suBlockStorageAdapter.properties

    override fun store(
        toStore: MutableBlock, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlock -> session.cacheStore(
                suBlockStorageAdapter,
                toStore, toStore.block
            )
            else -> suBlockStorageAdapter.store(toStore, session)
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlock, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suBlockStorageAdapter
        )

}