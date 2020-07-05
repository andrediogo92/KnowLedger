package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.coinbase.Coinbase
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.witness.factory.HashedWitnessFactory

internal class CoinbaseFactoryImpl(
    private val merkleTreeFactory: MerkleTreeFactory,
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory,
    private val witnessFactory: HashedWitnessFactory
) : CoinbaseFactory {
    override fun create(
        merkleTree: MutableMerkleTree,
        header: MutableCoinbaseHeader,
        witnesses: MutableSortedList<MutableWitness>
    ): MutableCoinbase = CoinbaseImpl(
        merkleTree = merkleTree,
        header = header,
        _witnesses = witnesses
    )

    override fun create(
        coinbase: Coinbase
    ): MutableCoinbase = with(coinbase) {
        create(
            merkleTree = merkleTreeFactory.create(merkleTree),
            header = coinbaseHeaderFactory.create(header),
            witnesses = witnesses.map {
                witnessFactory.create(it)
            }.toMutableSortedListFromPreSorted()
        )
    }

    override fun create(
        other: MutableCoinbase
    ): MutableCoinbase = with(other) {
        create(
            merkleTree = merkleTreeFactory.create(merkleTree),
            header = coinbaseHeaderFactory.create(header),
            witnesses = witnesses.map {
                witnessFactory.create(it)
            }.toMutableSortedListFromPreSorted()
        )
    }

}