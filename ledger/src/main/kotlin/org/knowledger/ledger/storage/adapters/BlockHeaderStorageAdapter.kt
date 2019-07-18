package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.SABlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.SUBlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.blockheader.StorageUnawareBlockHeader

object BlockHeaderStorageAdapter : LedgerStorageAdapter<BlockHeader> {
    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG,
            "hashId" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "ledgerParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nanos" to StorageType.INTEGER,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: BlockHeader,
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlockHeader ->
                SABlockHeaderStorageAdapter.store(toStore, session)
            is StorageUnawareBlockHeader ->
                SUBlockHeaderStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<BlockHeader, LoadFailure> =
        SABlockHeaderStorageAdapter.load(ledgerHash, element)

}