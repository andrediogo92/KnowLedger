package org.knowledger.ledger.storage.block.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.adapters.AdapterCollection
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.StorageAwareBlockImpl
import org.knowledger.ledger.storage.block.header.factory.StorageAwareBlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.StorageAwareCoinbaseFactory
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTreeFactory
import org.knowledger.ledger.storage.transaction.factory.StorageAwareTransactionFactory

internal class StorageAwareBlockFactory(
    private val adapterCollection: AdapterCollection,
    coinbaseFactory: StorageAwareCoinbaseFactory,
    transactionFactory: StorageAwareTransactionFactory,
    blockHeaderFactory: StorageAwareBlockHeaderFactory,
    merkleTreeFactory: StorageAwareMerkleTreeFactory,
    constructor: FactoryConstructor
) : BlockFactory {
    private val blockFactory = constructor(
        coinbaseFactory, transactionFactory,
        blockHeaderFactory, merkleTreeFactory
    )

    override fun create(
        transactions: MutableSortedList<MutableTransaction>,
        coinbase: MutableCoinbase,
        header: MutableBlockHeader,
        merkleTree: MutableMerkleTree
    ): MutableBlock = StorageAwareBlockImpl(
        adapterCollection, blockFactory.create(
            transactions, coinbase, header, merkleTree
        )
    )

    override fun create(other: MutableBlock): MutableBlock =
        StorageAwareBlockImpl(
            adapterCollection, blockFactory.create(other)
        )

    override fun create(block: Block): MutableBlock =
        StorageAwareBlockImpl(
            adapterCollection, blockFactory.create(block)
        )
}