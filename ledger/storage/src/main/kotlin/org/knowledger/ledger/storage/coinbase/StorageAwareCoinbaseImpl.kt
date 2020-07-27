package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal class StorageAwareCoinbaseImpl(
    override val coinbase: MutableCoinbase
) : StorageAwareCoinbase, MutableCoinbase by coinbase {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.Linked<MutableCoinbaseHeader>(
            "coinbaseHeader", AdapterIds.CoinbaseHeader
        ), StoragePairs.Linked<MutableMerkleTree>(
            "merkleTree", AdapterIds.MerkleTree
        ), StoragePairs.LinkedList<MutableWitness>(
            "witnesses", AdapterIds.Witness
        )
    )

    override fun addToOutputs(witness: MutableWitness) {
        coinbase.addToOutputs(witness)
        invalidated.replaceUnchecked(2, mutableWitnesses)
    }

    override fun equals(other: Any?): Boolean =
        coinbase == other

    override fun hashCode(): Int =
        coinbase.hashCode()
}