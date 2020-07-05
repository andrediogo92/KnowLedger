package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.block.header.BlockHeader
import org.knowledger.ledger.storage.block.header.BlockHeaderImpl
import org.knowledger.ledger.storage.block.header.HashedBlockHeader
import org.knowledger.ledger.storage.block.header.HashedBlockHeaderImpl

internal object HashedBlockHeaderFactoryImpl : HashedBlockHeaderFactory {
    private fun generateHash(
        chainId: ChainId, previousHash: Hash,
        blockParams: BlockParams, merkleRoot: Hash,
        seconds: Long, nonce: Long,
        hasher: Hashers, encoder: BinaryFormat
    ): Hash =
        generateHash(
            BlockHeaderImpl(
                chainId = chainId, previousHash = previousHash,
                params = blockParams, merkleRoot = merkleRoot,
                seconds = seconds, nonce = nonce
            ), hasher, encoder
        )

    private fun generateHash(
        blockHeader: BlockHeader,
        hasher: Hashers, encoder: BinaryFormat
    ): Hash =
        blockHeader.calculateHash(
            hasher, encoder
        )

    override fun create(
        blockHeader: BlockHeader, hasher: Hashers,
        encoder: BinaryFormat
    ): HashedBlockHeaderImpl {
        val hash = generateHash(
            blockHeader, hasher, encoder
        )
        return create(
            blockHeader.chainId, blockHeader.params,
            blockHeader.previousHash, hash,
            blockHeader.merkleRoot, blockHeader.seconds,
            blockHeader.nonce
        )
    }

    override fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hasher: Hashers,
        encoder: BinaryFormat, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): HashedBlockHeaderImpl {
        val hash = generateHash(
            chainId, previousHash, blockParams,
            merkleRoot, seconds, nonce,
            hasher, encoder
        )
        return create(
            chainId, blockParams, previousHash,
            hash, merkleRoot, seconds, nonce
        )
    }

    override fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hash: Hash, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): HashedBlockHeaderImpl =
        HashedBlockHeaderImpl(
            chainId = chainId, params = blockParams,
            _previousHash = previousHash, _hash = hash,
            _merkleRoot = merkleRoot, _seconds = seconds,
            _nonce = nonce
        )

    override fun create(
        blockHeader: HashedBlockHeader
    ): HashedBlockHeaderImpl =
        HashedBlockHeaderImpl(
            chainId = blockHeader.chainId,
            params = blockHeader.params,
            _previousHash = blockHeader.previousHash,
            _hash = blockHeader.hash,
            _merkleRoot = blockHeader.merkleRoot,
            _seconds = blockHeader.seconds,
            _nonce = blockHeader.nonce
        )
}