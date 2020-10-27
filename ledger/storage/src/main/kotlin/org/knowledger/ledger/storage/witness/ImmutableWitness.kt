@file:UseSerializers(HashSerializer::class, PayoutSerializer::class, SortedListSerializer::class)

package org.knowledger.ledger.storage.witness

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.serial.SortedListSerializer
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput

@Serializable
@SerialName("Witness")
data class ImmutableWitness(
    override val hash: Hash,
    override val publicKey: EncodedPublicKey,
    override val previousWitnessIndex: Int,
    override val previousCoinbase: Hash,
    override val payout: Payout,
    @SerialName("transactionOutputs")
    val immutableTransactionOutputs: SortedList<ImmutableTransactionOutput>
) : HashedWitness {
    @Suppress("UNCHECKED_CAST")
    override val transactionOutputs: SortedList<TransactionOutput>
        get() = immutableTransactionOutputs as SortedList<TransactionOutput>
}