package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.Serializable
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.core.base.data.truncatedHexString
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.indexed
import org.tinylog.kotlin.Logger

internal class CoinbaseImpl(
    override val merkleTree: MutableMerkleTree,
    override val header: MutableCoinbaseHeader,
    @Serializable(with = SortedListSerializer::class)
    private val _witnesses: MutableSortedList<MutableWitness> = mutableSortedListOf()
) : MutableCoinbase {
    @Suppress("UNCHECKED_CAST")
    override val witnesses: SortedList<Witness>
        get() = _witnesses as SortedList<Witness>

    override val mutableWitnesses: SortedList<MutableWitness>
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
        physicalData: PhysicalData,
        latestKnown: PhysicalData?,
        dataFormula: DataFormula
    ): Payout =
        //None are known for this area.
        if (latestKnown == null) {
            header.coinbaseParams.calculatePayout(
                physicalData,
                dataFormula
            )
        } else {
            header.coinbaseParams.calculatePayout(
                physicalData,
                latestKnown,
                dataFormula
            )
        }


}