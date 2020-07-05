package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.service.CloningFactory
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness

internal interface CoinbaseFactory : CloningFactory<MutableCoinbase> {
    fun create(
        merkleTree: MutableMerkleTree,
        header: MutableCoinbaseHeader,
        witnesses: MutableSortedList<MutableWitness>
    ): MutableCoinbase

    fun create(
        coinbase: Coinbase
    ): MutableCoinbase
}