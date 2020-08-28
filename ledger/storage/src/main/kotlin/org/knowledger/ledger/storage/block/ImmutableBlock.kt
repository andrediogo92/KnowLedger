@file:UseSerializers(SortedListSerializer::class)

package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.serial.SortedListSerializer
import org.knowledger.ledger.storage.transaction.ImmutableTransaction

@Serializable
@SerialName("Block")
data class ImmutableBlock(
    override val blockHeader: ImmutableBlockHeader,
    override val coinbase: ImmutableCoinbase,
    override val merkleTree: ImmutableMerkleTree,
    @SerialName("transactions")
    internal val immutableTransactions: SortedList<ImmutableTransaction>,
    override val approximateSize: Int,
) : Block {
    @Suppress("UNCHECKED_CAST")
    override val transactions: SortedList<Transaction>
        get() = immutableTransactions as SortedList<Transaction>
}