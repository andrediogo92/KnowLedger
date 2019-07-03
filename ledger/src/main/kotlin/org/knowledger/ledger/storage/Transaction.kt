package org.knowledger.ledger.storage

import com.squareup.moshi.JsonClass
import org.knowledger.common.Sizeable
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hashed
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.flattenBytes
import org.knowledger.common.misc.generateSignature
import org.knowledger.common.misc.verifyECDSASig
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.data.MerkleTree
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.openjdk.jol.info.ClassLayout
import java.security.PrivateKey
import java.security.PublicKey

@JsonClass(generateAdapter = true)
data class Transaction(
    val chainId: ChainId,
    // Agent's pub key.
    val publicKey: PublicKey,
    val data: PhysicalData,
    // This is to identify unequivocally an agent.
    internal var signatureInternal: ByteArray,
    internal var hash: Hash,
    @Transient
    val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
) : Sizeable,
    Hashed,
    Hashable,
    LedgerContract {


    val signature: ByteArray
        get() = signatureInternal

    override val hashId: Hash
        get() = hash

    /**
     * Calculate the approximate size of the transaction.
     *
     * @return The size of the transaction in bytes.
     */
    override val approximateSize: Long
        get() = ClassLayout
            .parseClass(this::class.java)
            .instanceSize() + data.approximateSize


    constructor(
        chainId: ChainId,
        identity: Identity,
        data: PhysicalData,
        hasher: Hasher
    ) : this(
        chainId,
        identity.publicKey,
        data,
        ByteArray(0),
        Hash.emptyHash,
        hasher
    ) {
        signatureInternal = generateSignature(
            identity.privateKey,
            identity.publicKey,
            data,
            hasher
        )
        updateHash(hasher)
    }

    constructor(
        chainId: ChainId,
        privateKey: PrivateKey,
        publicKey: PublicKey,
        data: PhysicalData,
        hasher: Hasher
    ) : this(
        chainId,
        publicKey,
        data,
        ByteArray(0),
        Hash.emptyHash,
        hasher
    ) {
        signatureInternal = generateSignature(
            privateKey,
            publicKey,
            data,
            hasher
        )
        updateHash(hasher)
    }

    /**
     * Hash is a cryptographic digest calculated from previous hashId,
     * internalNonce, internalTimestamp, [MerkleTree]'s root
     * and each [Transaction]'s hashId.
     */
    fun updateHash(hasher: Hasher) {
        hash = digest(hasher)
    }

    /**
     * Verifies the value we signed hasn't been
     * tampered with.
     *
     * @return Whether the value was signed with the
     * corresponding private key.
     */
    fun verifySignature(): Boolean {
        return verifyECDSASig(
            publicKey,
            publicKey.encoded + data.digest(
                hasher
            ).bytes,
            signature
        )
    }

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(): Boolean {
        return verifySignature()
    }


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                publicKey.encoded,
                data.digest(c).bytes,
                signature
            )
        )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false

        if (!hashId.contentEquals(other.hashId))
            return false
        if (!publicKey.encoded!!.contentEquals(
                other.publicKey.encoded!!
            )
        ) return false
        if (data != other.data) return false
        if (!signature.contentEquals(
                other.signature
            )
        ) return false
        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + hashId.contentHashCode()
        return result
    }
}
