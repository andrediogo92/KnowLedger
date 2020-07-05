package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.block.header.BlockHeader
import org.knowledger.ledger.storage.block.header.HashedBlockHeader
import org.knowledger.ledger.storage.block.header.StorageAwareBlockHeaderImpl

internal class StorageAwareBlockHeaderFactory(
    private val factory: HashedBlockHeaderFactory =
        HashedBlockHeaderFactoryImpl
) : HashedBlockHeaderFactory {

    override fun create(
        blockHeader: BlockHeader, hasher: Hashers,
        encoder: BinaryFormat
    ): StorageAwareBlockHeaderImpl =
        StorageAwareBlockHeaderImpl(
            factory.create(blockHeader, hasher, encoder)
        )

    override fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hasher: Hashers,
        encoder: BinaryFormat, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): StorageAwareBlockHeaderImpl =
        StorageAwareBlockHeaderImpl(
            factory.create(
                chainId, blockParams,
                previousHash, hasher,
                encoder, merkleRoot,
                seconds, nonce
            )
        )

    override fun create(
        chainId: ChainId, blockParams: BlockParams,
        previousHash: Hash, hash: Hash, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): StorageAwareBlockHeaderImpl =
        StorageAwareBlockHeaderImpl(
            factory.create(
                chainId, blockParams, previousHash,
                hash, merkleRoot, seconds, nonce
            )
        )

    override fun create(
        blockHeader: HashedBlockHeader
    ): StorageAwareBlockHeaderImpl =
        StorageAwareBlockHeaderImpl(
            factory.create(blockHeader)
        )
}