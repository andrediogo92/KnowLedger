@file:UseSerializers(PublicKeySerializer::class)

package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.serial.PublicKeySerializer
import java.math.BigDecimal
import java.security.PublicKey

/**
 * TransactionOutput contains transaction hashes used
 * for calculating payout and the cumulative payout
 * for the publickey in the current containing
 * coinbase.
 */
@Serializable
@SerialName("TransactionOutput")
internal data class TransactionOutputImpl(
    override val publicKey: PublicKey,
    override val previousCoinbase: Hash,
    @SerialName("payout")
    private var _payout: Payout,
    @SerialName("transactionHashes")
    private var _transactionHashes: MutableSet<Hash>
) : TransactionOutput {
    override val payout: Payout
        get() = _payout

    override val transactionHashes: Set<Hash>
        get() = _transactionHashes

    constructor(
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

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

}
