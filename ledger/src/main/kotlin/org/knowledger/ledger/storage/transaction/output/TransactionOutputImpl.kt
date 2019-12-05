@file:UseSerializers(PublicKeySerializer::class, HashSerializer::class, PayoutSerializer::class)

package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.data.Payout
import java.math.BigDecimal
import java.security.PublicKey

/**
 * TransactionOutput contains transaction hashes used
 * for calculating payout and the cumulative payout
 * for the publickey in the current containing
 * coinbase.
 */
@Serializable
internal data class TransactionOutputImpl(
    override val publicKey: PublicKey,
    override val previousCoinbase: Hash,
    private var _payout: Payout,
    private var _transactionHashes: MutableSet<Hash>
) : TransactionOutput {
    override val payout: Payout
        get() = _payout

    override val transactionHashes: Set<Hash>
        get() = _transactionHashes

    internal constructor(
        publicKey: PublicKey, previousCoinbase: Hash,
        payout: Payout, newTransaction: Hash,
        previousTransaction: Hash
    ) : this(
        publicKey = publicKey, previousCoinbase = previousCoinbase,
        _payout = Payout(BigDecimal.ZERO),
        _transactionHashes = mutableSetOf<Hash>()
    ) {
        addToPayout(payout, newTransaction, previousTransaction)
    }

    override fun clone(): TransactionOutputImpl =
        copy()


    override fun addToPayout(
        payout: Payout,
        newTransaction: Hash,
        previousTransaction: Hash
    ) {
        _transactionHashes.add(
            previousTransaction + newTransaction
        )
        _payout += payout
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionOutput) return false

        if (publicKey != other.publicKey) return false
        if (previousCoinbase != other.previousCoinbase) return false
        if (_payout != other.payout) return false
        if (_transactionHashes != other.transactionHashes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + previousCoinbase.hashCode()
        result = 31 * result + _payout.hashCode()
        result = 31 * result + _transactionHashes.hashCode()
        return result
    }


}
