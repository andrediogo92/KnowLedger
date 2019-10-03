package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.SABlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.SUHBlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader

object BlockHeaderStorageAdapter : LedgerStorageAdapter<HashedBlockHeader> {
    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: HashedBlockHeader,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlockHeader ->
                SABlockHeaderStorageAdapter.store(toStore, session)
            is HashedBlockHeaderImpl ->
                SUHBlockHeaderStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedBlockHeader, LoadFailure> =
        SABlockHeaderStorageAdapter.load(ledgerHash, element)

}