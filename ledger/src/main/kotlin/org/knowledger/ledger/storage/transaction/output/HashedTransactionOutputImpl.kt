package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.storage.HashUpdateable
import java.security.PublicKey

@Serializable
@SerialName("HashedTransactionOutput")
internal data class HashedTransactionOutputImpl(
    val transactionOutput: TransactionOutputImpl,
    @SerialName("hash")
    private var _hash: Hash? = null,
    @Transient
    var hasher: Hashers = DEFAULT_HASHER,
    @Transient
    var cbor: Cbor = Cbor.plain
) : HashedTransactionOutput,
    HashUpdateable,
    TransactionOutput by transactionOutput {
    @Transient
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, cbor)

    override val hash
        get() = _hash ?: recalculateHash(hasher, cbor)


    constructor(
        transactionOutput: PublicKey,
        previousUTXO: Hash,
        payout: Payout,
        newTransaction: Hash,
        previousTransaction: Hash,
        hasher: Hashers,
        cbor: Cbor
    ) : this(
        transactionOutput = TransactionOutputImpl(
            transactionOutput,
            previousUTXO,
            payout,
            newTransaction,
            previousTransaction
        ),
        hasher = hasher,
        cbor = cbor
    ) {
        updateHash(hasher, cbor)
    }

    constructor(
        publicKey: PublicKey, previousCoinbase: Hash,
        payout: Payout, transactionSet: MutableSet<Hash>,
        hash: Hash, hasher: Hashers, cbor: Cbor
    ) : this(
        TransactionOutputImpl(
            publicKey, previousCoinbase, payout,
            transactionSet
        ), hash, hasher, cbor
    )


    override fun updateHash(
        hasher: Hasher, cbor: Cbor
    ) {
        val bytes = transactionOutput.serialize(cbor)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun recalculateSize(hasher: Hasher, cbor: Cbor): Long {
        updateHash(hasher, cbor)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hasher, cbor: Cbor
    ): Hash {
        updateHash(hasher, cbor)
        return _hash as Hash
    }

    override fun addToPayout(payout: Payout, tx: Hash, prev: Hash) {
        transactionOutput.addToPayout(payout, tx, prev)
        if (cachedSize != null) {
            cachedSize = (cachedSize as Long) +
                    tx.bytes.size.toLong() +
                    prev.bytes.size.toLong()
        }
        updateHash(hasher, cbor)
    }

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedTransactionOutputImpl) return false

        if (transactionOutput != other.transactionOutput) return false
        if (_hash != other._hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactionOutput.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}