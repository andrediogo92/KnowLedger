@file:UseSerializers(SortedListSerializer::class)

package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.transaction.ImmutableTransaction

@Serializable
@SerialName("Block")
data class ImmutableBlock(
    @SerialName("transactions")
    val immutableTransactions: SortedList<ImmutableTransaction>,
    override val coinbase: ImmutableCoinbase,
    override val header: ImmutableBlockHeader,
    override val merkleTree: ImmutableMerkleTree,
    override val approximateSize: Int
) : Block {
    @Suppress("UNCHECKED_CAST")
    override val transactions: SortedList<Transaction>
        get() = immutableTransactions as SortedList<Transaction>
}