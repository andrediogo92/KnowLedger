package org.knowledger.ledger.storage.pools.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.storage.BlockPool
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockPool

interface BlockPoolFactory : CloningFactory<MutableBlockPool> {
    fun create(
        chainId: ChainId,
        blocks: MutableSortedList<MutableBlock> =
            mutableSortedListOf(),
    ): MutableBlockPool

    fun create(pool: BlockPool): MutableBlockPool
}