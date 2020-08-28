@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.block.header

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.config.block.ImmutableBlockParams

@Serializable
@SerialName("BlockHeader")
data class ImmutableBlockHeader(
    override val chainHash: Hash,
    override val hash: Hash,
    override val merkleRoot: Hash,
    override val previousHash: Hash,
    override val blockParams: ImmutableBlockParams,
    override val seconds: Long,
    override val nonce: Long,
) : HashedBlockHeader