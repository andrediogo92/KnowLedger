package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.block.header.BlockHeader
import org.knowledger.ledger.storage.block.header.HashedBlockHeader
import org.knowledger.ledger.storage.block.header.MutableHashedBlockHeader
import java.time.Instant

internal interface HashedBlockHeaderFactory {
    fun create(
        blockHeader: BlockHeader,
        hasher: Hashers,
        encoder: BinaryFormat
    ): MutableHashedBlockHeader

    fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hasher: Hashers,
        encoder: BinaryFormat,
        merkleRoot: Hash = Hash.emptyHash,
        seconds: Long = Instant.now().epochSecond,
        nonce: Long = Long.MIN_VALUE
    ): MutableHashedBlockHeader

    fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hash: Hash,
        merkleRoot: Hash = Hash.emptyHash,
        seconds: Long = Instant.now().epochSecond,
        nonce: Long = Long.MIN_VALUE
    ): MutableHashedBlockHeader

    fun create(
        blockHeader: HashedBlockHeader
    ): MutableHashedBlockHeader

}