package org.knowledger.ledger.storage.coinbase.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness

@OptIn(ExperimentalSerializationApi::class)
interface CoinbaseFactory : CloningFactory<MutableCoinbase> {
    fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): MutableCoinbase

    fun create(
        coinbaseHeader: MutableCoinbaseHeader, merkleTree: MutableMerkleTree,
        witnesses: MutableSortedList<MutableWitness>,
    ): MutableCoinbase

    fun create(coinbase: Coinbase): MutableCoinbase
}