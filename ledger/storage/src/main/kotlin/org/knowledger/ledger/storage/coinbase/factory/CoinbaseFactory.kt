package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness

interface CoinbaseFactory : CloningFactory<MutableCoinbase> {
    fun create(
        coinbaseHeader: MutableCoinbaseHeader,
        merkleTree: MutableMerkleTree,
        witnesses: MutableSortedList<MutableWitness>
    ): MutableCoinbase

    fun create(
        coinbase: Coinbase
    ): MutableCoinbase
}