package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash
import java.time.Instant

@Serializable
@SerialName("BlockHeader")
internal data class BlockHeaderImpl(
    override val chainId: ChainId,
    override val previousHash: Hash,
    override val params: BlockParams,
    @SerialName("merkleRoot")
    internal var _merkleRoot: Hash = Hash.emptyHash,
    override val seconds: Long = Instant.now().epochSecond,
    @SerialName("nonce")
    internal var _nonce: Long = Long.MIN_VALUE
) : BlockHeader {
    override val nonce: Long
        get() = _nonce

    override val merkleRoot: Hash
        get() = _merkleRoot

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)


    override fun clone(): BlockHeader =
        copy()
}