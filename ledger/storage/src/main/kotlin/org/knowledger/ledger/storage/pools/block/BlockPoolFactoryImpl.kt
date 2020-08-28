package org.knowledger.ledger.storage.pools.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.storage.BlockPool
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockPool

internal class BlockPoolFactoryImpl : BlockPoolFactory {
    override fun create(chainId: ChainId, blocks: MutableSortedList<MutableBlock>): BlockPoolImpl =
        BlockPoolImpl(chainId, blocks)

    override fun create(pool: BlockPool): MutableBlockPool =
        with(pool) { create(chainId, blocks.toMutableSortedListFromPreSorted()) }

    override fun create(other: MutableBlockPool): BlockPoolImpl =
        with(other) { create(chainId, mutableBlocks) }
}