package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.Indexed
import java.security.PrivateKey
import java.security.PublicKey

internal data class HashedTransactionImpl(
    val signedTransaction: SignedTransactionImpl,
    private var _index: Int = -1,
    private var _hash: Hash? = null
) : HashedTransaction, Indexed, HashUpdateable,
    SignedTransaction by signedTransaction {
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: throw UninitializedPropertyAccessException("Approximate size never calculated")

    override val hash: Hash
        get() = _hash ?: throw UninitializedPropertyAccessException("Hash was not initialized")

    override val index: Int
        get() = _index

    internal constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hasher: Hashers,
        encoder: BinaryFormat, index: Int = -1
    ) : this(
        signedTransaction = SignedTransactionImpl(
            privateKey = privateKey,
            publicKey = publicKey,
            data = data, encoder = encoder
        ), _index = index
    ) {
        updateHash(hasher, encoder)
    }

    internal constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray, hash: Hash, index: Int = -1
    ) : this(
        signedTransaction = SignedTransactionImpl(
            publicKey = publicKey, data = data,
            signature = signature
        ), _index = index, _hash = hash
    )

    internal constructor(
        identity: Identity, data: PhysicalData,
        hasher: Hashers, encoder: BinaryFormat,
        index: Int = -1
    ) : this(
        privateKey = identity.privateKey,
        publicKey = identity.publicKey,
        data = data, hasher = hasher,
        encoder = encoder, index = index
    )

    override fun markIndex(index: Int) {
        _index = index
    }

    override fun clone(): HashedTransactionImpl =
        copy(
            signedTransaction = signedTransaction.clone(),
            _index = -1
        )

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

    override fun updateHash(
        hasher: Hashers, encoder: BinaryFormat
    ) {
        val bytes = signedTransaction.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedTransaction) return false

        if (signedTransaction != other) return false
        if (_hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signedTransaction.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}