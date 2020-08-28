package org.knowledger.ledger.storage.coinbase.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbaseImpl

@OptIn(ExperimentalSerializationApi::class)
internal class StorageAwareCoinbaseFactory(
    private val coinbaseFactory: CoinbaseFactory,
) : CoinbaseFactory {
    private fun createSA(mutableCoinbase: MutableCoinbase): StorageAwareCoinbaseImpl =
        StorageAwareCoinbaseImpl(mutableCoinbase)

    override fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): MutableCoinbase =
        createSA(coinbaseFactory.create(coinbaseParams, hashers, encoder))

    override fun create(
        coinbaseHeader: MutableCoinbaseHeader, merkleTree: MutableMerkleTree,
        witnesses: MutableSortedList<MutableWitness>,
    ): StorageAwareCoinbaseImpl =
        createSA(coinbaseFactory.create(coinbaseHeader, merkleTree, witnesses))

    override fun create(coinbase: Coinbase): StorageAwareCoinbaseImpl =
        createSA(coinbaseFactory.create(coinbase))

    override fun create(other: MutableCoinbase): StorageAwareCoinbaseImpl =
        createSA(coinbaseFactory.create(other))
}