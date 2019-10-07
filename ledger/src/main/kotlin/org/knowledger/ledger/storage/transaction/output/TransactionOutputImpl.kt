@file:UseSerializers(PublicKeySerializer::class)

package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.PublicKeySerializer
import org.knowledger.ledger.data.Hash
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

}
