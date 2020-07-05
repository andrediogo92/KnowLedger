package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.adapters.AdapterCollection
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.coinbase.Coinbase
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbaseImpl
import org.knowledger.ledger.storage.coinbase.header.factory.StorageAwareCoinbaseHeaderFactory
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTreeFactory
import org.knowledger.ledger.storage.witness.factory.StorageAwareWitnessFactory

internal class StorageAwareCoinbaseFactory(
    private val adapterCollection: AdapterCollection,
    merkleTreeFactory: StorageAwareMerkleTreeFactory,
    coinbaseHeaderFactory: StorageAwareCoinbaseHeaderFactory,
    witnessFactory: StorageAwareWitnessFactory,
    constructor: FactoryConstructor
) : CoinbaseFactory {
    private val coinbaseFactory: CoinbaseFactory = constructor(
        merkleTreeFactory, coinbaseHeaderFactory, witnessFactory
    )

    private fun createSA(
        mutableCoinbase: MutableCoinbase
    ): StorageAwareCoinbaseImpl = StorageAwareCoinbaseImpl(
        adapterCollection, mutableCoinbase
    )

    override fun create(
        merkleTree: MutableMerkleTree,
        header: MutableCoinbaseHeader,
        witnesses: MutableSortedList<MutableWitness>
    ): StorageAwareCoinbaseImpl = createSA(
        coinbaseFactory.create(
            merkleTree, header, witnesses
        )
    )

    override fun create(
        coinbase: Coinbase
    ): StorageAwareCoinbaseImpl = createSA(
        coinbaseFactory.create(coinbase)
    )

    override fun create(
        other: MutableCoinbase
    ): StorageAwareCoinbaseImpl = createSA(
        coinbaseFactory.create(other)
    )
}