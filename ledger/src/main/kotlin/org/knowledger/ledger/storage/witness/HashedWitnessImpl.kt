package org.knowledger.ledger.storage.witness

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.TransactionOutput

internal data class HashedWitnessImpl(
    val witness: WitnessImpl,
    private var _hash: Hash? = null,
    @Transient
    var hasher: Hashers = DEFAULT_HASHER,
    @Transient
    var encoder: BinaryFormat = Cbor
) : HashedWitness,
    HashUpdateable,
    Witness by witness {
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, encoder)

    override val hash
        get() = _hash ?: recalculateHash(hasher, encoder)


    internal constructor(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash,
        hasher: Hashers, encoder: BinaryFormat
    ) : this(
        witness = WitnessImpl(
            publicKey = publicKey,
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = previousCoinbase,
            payout = payout,
            newIndex = newIndex,
            newTransaction = newTransaction,
            previousBlock = previousBlock,
            previousIndex = previousIndex,
            previousTransaction = previousTransaction
        ),
        hasher = hasher,
        encoder = encoder
    ) {
        updateHash(hasher, encoder)
    }

    internal constructor(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, hasher: Hashers, encoder: BinaryFormat
    ) : this(
        witness = WitnessImpl(
            publicKey = publicKey,
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = previousCoinbase,
            _payout = payout,
            _transactionOutputs = transactionOutputs
        ), _hash = hash, hasher = hasher, encoder = encoder
    )

    override fun clone(): HashedWitnessImpl =
        copy(
            witness = witness.clone()
        )

    override fun updateHash(
        hasher: Hashers, encoder: BinaryFormat
    ) {
        val bytes = witness.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun recalculateSize(
        hasher: Hashers, encoder: BinaryFormat
    ): Long {
        updateHash(hasher, encoder)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hashers, encoder: BinaryFormat
    ): Hash {
        updateHash(hasher, encoder)
        return _hash as Hash
    }

    override fun addToPayout(
        payout: Payout, newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash
    ) {
        witness.addToPayout(
            payout = payout, newIndex = newIndex,
            newTransaction = newTransaction,
            previousBlock = previousBlock,
            previousIndex = previousIndex,
            previousTransaction = previousTransaction
        )
        if (cachedSize != null) {
            cachedSize = (cachedSize as Long) +
                    newTransaction.bytes.size.toLong() +
                    previousTransaction.bytes.size.toLong()
        }
        updateHash(hasher, encoder)
    }

    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) return true
        if (other !is HashedWitness) return false

        if (witness != other) return false
        if (_hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = witness.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}