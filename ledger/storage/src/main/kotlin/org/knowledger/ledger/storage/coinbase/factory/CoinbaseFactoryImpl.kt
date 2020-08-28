package org.knowledger.ledger.storage.coinbase.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.witness.factory.WitnessFactory

@OptIn(ExperimentalSerializationApi::class)
internal class CoinbaseFactoryImpl(
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory,
    private val merkleTreeFactory: MerkleTreeFactory,
    private val witnessFactory: WitnessFactory,
) : CoinbaseFactory {
    override fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): CoinbaseImpl = create(
        coinbaseHeaderFactory.create(coinbaseParams, hashers, encoder),
        merkleTreeFactory.create(hashers), mutableSortedListOf()
    )

    override fun create(
        coinbaseHeader: MutableCoinbaseHeader, merkleTree: MutableMerkleTree,
        witnesses: MutableSortedList<MutableWitness>,
    ): CoinbaseImpl = CoinbaseImpl(coinbaseHeader, merkleTree, witnesses)

    override fun create(coinbase: Coinbase): CoinbaseImpl =
        with(coinbase) {
            create(
                coinbaseHeaderFactory.create(coinbaseHeader), merkleTreeFactory.create(merkleTree),
                witnesses.map(witnessFactory::create).toMutableSortedListFromPreSorted()
            )
        }

    override fun create(other: MutableCoinbase): CoinbaseImpl =
        create(other as Coinbase)

}