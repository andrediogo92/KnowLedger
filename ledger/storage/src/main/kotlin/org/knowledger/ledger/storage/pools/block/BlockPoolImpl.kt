package org.knowledger.ledger.storage.pools.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked
import org.knowledger.ledger.storage.results.BlockFailure

internal data class BlockPoolImpl(
    override val chainId: ChainId,
    override val mutableBlocks: MutableSortedList<MutableBlock>,
) : StorageAwareBlockPool {
    override val lock: BooleanLocking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(StoragePairs.LinkedList<MutableBlock>("blocks", AdapterIds.Block))

    override val blocks: SortedList<MutableBlock>
        get() = mutableBlocks

    override fun refresh(hash: Hash): Outcome<MutableBlockHeader, BlockFailure> =
        get(hash)?.let { block ->
            block.newExtraNonce()
            block.blockHeader.ok()
        } ?: BlockFailure.NoBlockForHash(hash).err()


    override operator fun plusAssign(block: MutableBlock) {
        mutableBlocks.add(block)
        if (id != null) {
            invalidated.replaceUnchecked(0, mutableBlocks)
        }
    }

    override operator fun minusAssign(block: MutableBlock) {
        mutableBlocks.remove(block)
        if (id != null) {
            invalidated.replaceUnchecked(0, mutableBlocks)
        }
    }
}

