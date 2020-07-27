package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.block.header.StorageAwareBlockHeaderImpl
import org.knowledger.ledger.storage.block.header.BlockHeader as UnhashedBlockHeader

internal class StorageAwareBlockHeaderFactory(
    private val factory: BlockHeaderFactory = HashedBlockHeaderFactory()
) : BlockHeaderFactory {

    private fun createSA(
        blockHeader: MutableBlockHeader
    ): StorageAwareBlockHeaderImpl =
        StorageAwareBlockHeaderImpl(blockHeader)

    override fun create(
        blockHeader: UnhashedBlockHeader, hasher: Hashers, encoder: BinaryFormat
    ): StorageAwareBlockHeaderImpl =
        createSA(factory.create(blockHeader, hasher, encoder))

    override fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        hasher: Hashers, encoder: BinaryFormat, merkleRoot: Hash,
        seconds: Long, nonce: Long
    ): StorageAwareBlockHeaderImpl = createSA(
        factory.create(
            chainHash, previousHash, blockParams, hasher,
            encoder, merkleRoot, seconds, nonce
        )
    )

    override fun create(
        chainHash: Hash, hash: Hash, previousHash: Hash, blockParams: BlockParams,
        merkleRoot: Hash, seconds: Long, nonce: Long
    ): StorageAwareBlockHeaderImpl = createSA(
        factory.create(
            chainHash, hash, previousHash, blockParams,
            merkleRoot, seconds, nonce
        )
    )

    override fun create(
        blockHeader: BlockHeader
    ): StorageAwareBlockHeaderImpl =
        createSA(factory.create(blockHeader))

    override fun create(
        other: MutableBlockHeader
    ): StorageAwareBlockHeaderImpl =
        createSA(factory.create(other))
}