@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.block.header

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.chainid.ImmutableChainId
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

@Serializable
@SerialName("BlockHeader")
data class ImmutableBlockHeader(
    override val chainId: ImmutableChainId,
    override val merkleRoot: Hash,
    override val previousHash: Hash,
    override val params: BlockParams,
    override val seconds: Long,
    override val nonce: Long,
    override val hash: Hash
) : HashedBlockHeader