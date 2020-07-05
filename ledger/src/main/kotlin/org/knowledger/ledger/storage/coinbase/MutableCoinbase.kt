package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness

internal interface MutableCoinbase : Coinbase, Sizeable {
    val mutableWitnesses: SortedList<MutableWitness>
    override val header: MutableCoinbaseHeader
    override val merkleTree: MutableMerkleTree

    fun addToOutputs(witness: MutableWitness)
}