package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.storage.HashUpdateable
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
@SerialName("HashedTransaction")
internal data class HashedTransactionImpl(
    val signedTransaction: SignedTransactionImpl,
    @SerialName("hash")
    internal var _hash: Hash? = null
) : HashedTransaction,
    HashUpdateable,
    SignedTransaction by signedTransaction {
    @Transient
    private var cachedSize: Long? = null

    override val approximateSize: Long?
        get() = cachedSize

    override val hash: Hash
        get() = _hash ?: throw UninitializedPropertyAccessException("Hash was not initialized")

    constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hasher: Hashers, cbor: Cbor
    ) : this(
        signedTransaction = SignedTransactionImpl(
            privateKey = privateKey,
            publicKey = publicKey,
            data = data, cbor = cbor
        )
    ) {
        updateHash(hasher, cbor)
    }

    constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray, hash: Hash
    ) : this(
        signedTransaction = SignedTransactionImpl(
            publicKey = publicKey, data = data,
            signature = signature
        ), _hash = hash
    )

    constructor(
        identity: Identity, data: PhysicalData,
        hasher: Hashers, cbor: Cbor
    ) : this(
        privateKey = identity.privateKey,
        publicKey = identity.publicKey,
        data = data, hasher = hasher,
        cbor = cbor
    )


    override fun recalculateSize(
        hasher: Hasher, cbor: Cbor
    ): Long {
        updateHash(hasher, cbor)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hasher, cbor: Cbor
    ): Hash {
        updateHash(hasher, cbor)
        return _hash as Hash
    }

    override fun updateHash(
        hasher: Hasher, cbor: Cbor
    ) {
        val bytes = signedTransaction.serialize(cbor)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedTransactionImpl) return false

        if (signedTransaction != other.signedTransaction) return false
        if (_hash != other._hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signedTransaction.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}