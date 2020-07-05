package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.PhysicalData
import java.security.PublicKey

internal data class HashedTransactionImpl(
    override val publicKey: PublicKey,
    override val data: PhysicalData,
    override val signature: EncodedSignature,
    private var _hash: Hash,
    private var cachedSize: Int,
    private var _index: Int = -1
) : MutableHashedTransaction {
    override val approximateSize: Int
        get() = cachedSize

    override val hash: Hash
        get() = _hash

    override val index: Int
        get() = _index


    override fun markIndex(index: Int) {
        _index = index
    }

    override fun updateHash(hash: Hash) {
        _hash = hash
    }

    override fun updateSize(size: Int) {
        cachedSize = size
    }

    override fun processTransaction(encoder: BinaryFormat): Boolean {
        return verifySignature(encoder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedTransaction) return false


        if (publicKey != other.publicKey) return false
        if (data != other.data) return false
        if (signature != other.signature) return false
        if (_hash != other.hash) return false
        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + _hash.hashCode()
        return result
    }


}