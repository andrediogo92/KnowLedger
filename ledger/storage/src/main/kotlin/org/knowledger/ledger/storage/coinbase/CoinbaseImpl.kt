package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.core.data.truncatedHexString
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.mutations.indexed
import org.tinylog.kotlin.Logger

internal class CoinbaseImpl(
    override val coinbaseHeader: MutableCoinbaseHeader,
    override val merkleTree: MutableMerkleTree,
    private val _witnesses: MutableSortedList<MutableWitness> = mutableSortedListOf(),
) : MutableCoinbase {
    @Suppress("UNCHECKED_CAST")
    override val witnesses: SortedList<Witness>
        get() = _witnesses as SortedList<Witness>

    override val mutableWitnesses: MutableSortedList<MutableWitness>
        get() = _witnesses


    override fun addToOutputs(witness: MutableWitness) {
        val index = _witnesses.addWithIndex(witness)
        if (index >= 0) {
            _witnesses.indexed(index, _witnesses.size)
        } else {
            Logger.error {
                "Witness ${witness.hash.truncatedHexString()} already exists in coinbase"
            }
        }
    }


    private fun checkInput(
        physicalData: PhysicalData, latestKnown: PhysicalData?, dataFormula: DataFormula,
    ): Payout =
        //None are known for this area.
        if (latestKnown == null) {
            coinbaseHeader.coinbaseParams.calculatePayout(physicalData, dataFormula)
        } else {
            coinbaseHeader.coinbaseParams.calculatePayout(physicalData, latestKnown, dataFormula)
        }


}