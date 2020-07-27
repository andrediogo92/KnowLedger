package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.core.data.Sizeable
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness

interface MutableCoinbase : Coinbase, Sizeable {
    val mutableWitnesses: MutableSortedList<MutableWitness>
    override val coinbaseHeader: MutableCoinbaseHeader
    override val merkleTree: MutableMerkleTree

    fun addToOutputs(witness: MutableWitness)
}