@file:UseSerializers(ChainIdByteSerializer::class)

package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.serial.internal.ChainIdByteSerializer
import java.time.Instant

@Serializable
internal data class BlockHeaderImpl(
    override val chainId: ChainId,
    override val previousHash: Hash,
    override val params: BlockParams,
    internal var _merkleRoot: Hash = Hash.emptyHash,
    override val seconds: Long = Instant.now().epochSecond,
    internal var _nonce: Long = Long.MIN_VALUE
) : BlockHeader {
    override val nonce: Long
        get() = _nonce

    override val merkleRoot: Hash
        get() = _merkleRoot

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)


    override fun clone(): BlockHeaderImpl =
        copy(
            chainId = chainId
        )
}