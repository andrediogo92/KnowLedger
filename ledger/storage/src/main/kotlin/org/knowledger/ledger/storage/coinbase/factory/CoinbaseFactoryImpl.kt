package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.witness.factory.WitnessFactory

internal class CoinbaseFactoryImpl(
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory,
    private val merkleTreeFactory: MerkleTreeFactory,
    private val witnessFactory: WitnessFactory
) : CoinbaseFactory {
    override fun create(
        coinbaseHeader: MutableCoinbaseHeader,
        merkleTree: MutableMerkleTree,
        witnesses: MutableSortedList<MutableWitness>
    ): CoinbaseImpl =
        CoinbaseImpl(coinbaseHeader, merkleTree, witnesses)

    override fun create(
        coinbase: Coinbase
    ): CoinbaseImpl = with(coinbase) {
        create(
            coinbaseHeaderFactory.create(coinbaseHeader),
            merkleTreeFactory.create(merkleTree),
            witnesses.map {
                witnessFactory.create(it)
            }.toMutableSortedListFromPreSorted()
        )
    }

    override fun create(
        other: MutableCoinbase
    ): CoinbaseImpl = create(other as Coinbase)

}