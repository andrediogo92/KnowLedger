package org.knowledger.ledger.storage.pools.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.results.BlockFailure

interface MutableBlockPool : BlockPool {
    val mutableBlocks: MutableSortedList<MutableBlock>

    fun refresh(hash: Hash): Outcome<MutableBlockHeader, BlockFailure>
    operator fun plusAssign(block: MutableBlock)
    operator fun minusAssign(block: MutableBlock)
}