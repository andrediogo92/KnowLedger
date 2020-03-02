@file:UseSerializers(HashSerializer::class, PayoutSerializer::class)
package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout

@Serializable
data class TransactionOutputImpl internal constructor(
    override val payout: Payout,
    override val prevTxBlock: Hash,
    override val prevTxIndex: Int,
    override val prevTx: Hash,
    override val txIndex: Int,
    override val tx: Hash
) : TransactionOutput {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionOutput) return false

        if (payout != other.payout) return false
        if (prevTxBlock != other.prevTxBlock) return false
        if (prevTxIndex != other.prevTxIndex) return false
        if (prevTx != other.prevTx) return false
        if (txIndex != other.txIndex) return false
        if (tx != other.tx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payout.hashCode()
        result = 31 * result + prevTxBlock.hashCode()
        result = 31 * result + prevTxIndex
        result = 31 * result + prevTx.hashCode()
        result = 31 * result + txIndex
        result = 31 * result + tx.hashCode()
        return result
    }


}