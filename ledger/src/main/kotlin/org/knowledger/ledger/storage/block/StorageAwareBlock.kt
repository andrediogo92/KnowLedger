package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.collections.copyMutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.updateLinked

@Serializable
@SerialName("StorageBlockWrapper")
internal class StorageAwareBlock private constructor(
    internal val block: BlockImpl
) : Block by block,
    StorageAware<Block> {
    internal constructor(
        adapterManager: AdapterManager,
        block: BlockImpl
    ) : this(block) {
        _invalidated = arrayOf(
            StoragePairs.Linked("header", adapterManager.blockHeaderStorageAdapter),
            StoragePairs.Linked("coinbase", adapterManager.coinbaseStorageAdapter),
            StoragePairs.Linked("merkleTree", adapterManager.merkleTreeStorageAdapter),
            StoragePairs.LinkedList("data", adapterManager.transactionStorageAdapter)
        )
    }

    internal constructor(
        adapterManager: AdapterManager,
        ledgerInfo: LedgerInfo,
        chainId: ChainId,
        previousHash: Hash,
        blockParams: BlockParams
    ) : this(
        adapterManager,
        BlockImpl(
            mutableSortedListOf(),
            StorageAwareCoinbase(
                ledgerInfo,
                adapterManager
            ),
            StorageAwareBlockHeader(
                chainId,
                ledgerInfo.hasher,
                ledgerInfo.encoder,
                previousHash,
                blockParams
            ),
            StorageAwareMerkleTree(ledgerInfo.hasher)
        )
    )


    @Transient
    override var id: StorageID? = null

    @Transient
    private var _invalidated: Array<StoragePairs<*>> = emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    override fun newExtraNonce(): Block {
        block.newExtraNonce()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, coinbase)
            invalidated.replace(2, merkleTree)
        }
        return this
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun plus(transaction: Transaction): Boolean {
        val result = block + transaction
        if (result && id != null) {
            invalidated.replace(3, transactions)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, merkleTree)
        }
    }

    override fun clone(): Block {
        return BlockImpl(
            transactions.copyMutableSortedList(
                Transaction::clone
            ),
            coinbase.clone(),
            header.clone(),
            merkleTree.clone()
        )
    }

    override fun equals(other: Any?): Boolean =
        block == other

    override fun hashCode(): Int =
        block.hashCode()
}