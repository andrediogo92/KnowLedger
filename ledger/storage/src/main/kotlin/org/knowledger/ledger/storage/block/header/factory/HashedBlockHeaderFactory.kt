package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.block.header.BlockHeaderImpl
import org.knowledger.ledger.storage.block.header.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.block.header.BlockHeader as UnhashedBlockHeader

internal class HashedBlockHeaderFactory : BlockHeaderFactory {
    private fun generateHash(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        merkleRoot: Hash, seconds: Long, nonce: Long,
        hashers: Hashers, encoder: BinaryFormat
    ): Hash = generateHash(
        BlockHeaderImpl(
            chainHash, merkleRoot, previousHash,
            blockParams, seconds, nonce
        ), hashers, encoder
    )

    private fun generateHash(
        blockHeader: UnhashedBlockHeader, hashers: Hashers,
        encoder: BinaryFormat
    ): Hash = blockHeader.calculateHash(hashers, encoder)

    override fun create(
        blockHeader: UnhashedBlockHeader, hasher: Hashers,
        encoder: BinaryFormat
    ): HashedBlockHeaderImpl {
        val hash = generateHash(
            blockHeader, hasher, encoder
        )
        return with(blockHeader) {
            create(
                chainHash, hash, previousHash,
                blockParams, merkleRoot, seconds, nonce
            )
        }
    }

    override fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        hasher: Hashers, encoder: BinaryFormat, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): HashedBlockHeaderImpl {
        val hash = generateHash(
            chainHash, previousHash, blockParams,
            merkleRoot, seconds, nonce,
            hasher, encoder
        )
        return create(
            chainHash, hash, previousHash, blockParams,
            merkleRoot, seconds, nonce
        )
    }

    override fun create(
        chainHash: Hash, hash: Hash, previousHash: Hash, blockParams: BlockParams,
        merkleRoot: Hash, seconds: Long, nonce: Long
    ): HashedBlockHeaderImpl = HashedBlockHeaderImpl(
        chainHash, hash, merkleRoot, previousHash,
        blockParams, seconds, nonce
    )

    override fun create(
        blockHeader: BlockHeader
    ): HashedBlockHeaderImpl = with(blockHeader) {
        create(
            chainHash, hash, previousHash, blockParams,
            merkleRoot, seconds, nonce
        )
    }

    override fun create(
        other: MutableBlockHeader
    ): HashedBlockHeaderImpl =
        create(other as BlockHeader)
}