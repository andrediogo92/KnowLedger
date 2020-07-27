package org.knowledger.ledger.storage.block.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.StorageAwareBlockImpl

internal class StorageAwareBlockFactory(
    private val blockFactory: BlockFactory
) : BlockFactory {
    private fun createSA(block: MutableBlock): StorageAwareBlockImpl =
        StorageAwareBlockImpl(block)

    override fun create(
        transactions: MutableSortedList<MutableTransaction>,
        coinbase: MutableCoinbase,
        header: MutableBlockHeader,
        merkleTree: MutableMerkleTree
    ): StorageAwareBlockImpl =
        createSA(blockFactory.create(transactions, coinbase, header, merkleTree))

    override fun create(
        other: MutableBlock
    ): StorageAwareBlockImpl =
        createSA(blockFactory.create(other))

    override fun create(
        block: Block
    ): StorageAwareBlockImpl =
        createSA(blockFactory.create(block))
}