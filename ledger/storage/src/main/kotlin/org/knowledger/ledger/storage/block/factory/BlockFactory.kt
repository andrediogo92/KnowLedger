package org.knowledger.ledger.storage.block.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction

interface BlockFactory : CloningFactory<MutableBlock> {
    fun create(
        transactions: MutableSortedList<MutableTransaction>,
        coinbase: MutableCoinbase,
        header: MutableBlockHeader,
        merkleTree: MutableMerkleTree
    ): MutableBlock

    fun create(
        block: Block
    ): MutableBlock
}