package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.storage.HashUpdateable
import java.security.PrivateKey
import java.security.PublicKey

internal data class HashedTransactionImpl(
    val signedTransaction: SignedTransactionImpl,
    internal var _hash: Hash? = null
) : HashedTransaction,
    HashUpdateable,
    SignedTransaction by signedTransaction {
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: throw UninitializedPropertyAccessException("Approximate size never calculated")

    override val hash: Hash
        get() = _hash ?: throw UninitializedPropertyAccessException("Hash was not initialized")

    internal constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hasher: Hashers, encoder: BinaryFormat
    ) : this(
        signedTransaction = SignedTransactionImpl(
            privateKey = privateKey,
            publicKey = publicKey,
            data = data, encoder = encoder
        )
    ) {
        updateHash(hasher, encoder)
    }

    internal constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray, hash: Hash
    ) : this(
        signedTransaction = SignedTransactionImpl(
            publicKey = publicKey, data = data,
            signature = signature
        ), _hash = hash
    )

    internal constructor(
        identity: Identity, data: PhysicalData,
        hasher: Hashers, encoder: BinaryFormat
    ) : this(
        privateKey = identity.privateKey,
        publicKey = identity.publicKey,
        data = data, hasher = hasher,
        encoder = encoder
    )

    override fun clone(): HashedTransactionImpl =
        copy(
            signedTransaction = signedTransaction.clone()
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