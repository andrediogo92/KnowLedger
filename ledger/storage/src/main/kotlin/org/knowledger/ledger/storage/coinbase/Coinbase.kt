package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.data.Sizeable
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.mutations.calculateWitnessesSize

interface Coinbase : Sizeable, LedgerContract {
    val coinbaseHeader: CoinbaseHeader
    val merkleTree: MerkleTree
    val witnesses: SortedList<Witness>

    fun findWitness(publicKey: EncodedPublicKey): Int =
        witnesses.binarySearch { witness ->
            witness.publicKey.compareTo(publicKey)
        }

    override val approximateSize: Int
        get() = witnesses.calculateWitnessesSize(coinbaseHeader.coinbaseParams.hashSize)
}