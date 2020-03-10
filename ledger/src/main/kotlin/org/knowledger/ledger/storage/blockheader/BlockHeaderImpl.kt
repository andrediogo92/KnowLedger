@file:UseSerializers(ChainIdByteSerializer::class, HashSerializer::class)

package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.binary.ChainIdByteSerializer
import org.knowledger.ledger.storage.NonceRegen
import java.time.Instant

@Serializable
internal data class BlockHeaderImpl(
    override val chainId: ChainId,
    override val previousHash: Hash,
    override val params: BlockParams,
    private var _merkleRoot: Hash = Hash.emptyHash,
    override val seconds: Long = Instant.now().epochSecond,
    private var _nonce: Long = Long.MIN_VALUE
) : BlockHeader, NonceRegen,
    NonceReset, MerkleTreeUpdate {
    override val nonce: Long
        get() = _nonce

    override val merkleRoot: Hash
        get() = _merkleRoot

    override fun serialize(
        encoder: BinaryFormat
    ): ByteArray =
        encoder.dump(serializer(), this)

    override fun updateMerkleTree(newRoot: Hash) {
        _merkleRoot = newRoot
    }

    override fun nonceReset() {
        _nonce = 0
    }

    override fun newNonce() {
        _nonce++
    }

    override fun clone(): BlockHeaderImpl =
        copy(
            chainId = chainId
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockHeader) return false

        if (chainId != other.chainId) return false
        if (previousHash != other.previousHash) return false
        if (params != other.params) return false
        if (_merkleRoot != other.merkleRoot) return false
        if (seconds != other.seconds) return false
        if (_nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + _merkleRoot.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + _nonce.hashCode()
        return result
    }


}