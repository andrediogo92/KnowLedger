package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.calculateWitnessesSize

interface Coinbase : Sizeable, LedgerContract {
    val header: CoinbaseHeader
    val witnesses: SortedList<Witness>
    val merkleTree: MerkleTree

    fun findWitness(tx: Transaction): Int =
        witnesses.binarySearch { witness ->
            witness.publicKey.compareTo(tx.publicKey.toEncoded())
        }

    override val approximateSize: Int
        get() = witnesses.calculateWitnessesSize(
            header.coinbaseParams.hashSize
        )
}