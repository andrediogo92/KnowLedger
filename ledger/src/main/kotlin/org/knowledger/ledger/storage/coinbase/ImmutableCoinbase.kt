@file:UseSerializers(SortedListSerializer::class)

package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.witness.ImmutableWitness

@Serializable
data class ImmutableCoinbase(
    override val header: ImmutableCoinbaseHeader,
    @SerialName("witnesses")
    val immutableWitnesses: SortedList<ImmutableWitness>,
    override val merkleTree: ImmutableMerkleTree
) : Coinbase {
    @Suppress("UNCHECKED_CAST")
    override val witnesses: SortedList<Witness>
        get() = immutableWitnesses as SortedList<Witness>
}