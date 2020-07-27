package org.knowledger.ledger.storage.block.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableBlockHeader
import java.time.Instant
import org.knowledger.ledger.storage.block.header.BlockHeader as UnhashedBlockHeader

interface BlockHeaderFactory : CloningFactory<MutableBlockHeader> {
    fun create(
        blockHeader: UnhashedBlockHeader, hasher: Hashers, encoder: BinaryFormat
    ): MutableBlockHeader

    fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        hasher: Hashers, encoder: BinaryFormat, merkleRoot: Hash = Hash.emptyHash,
        seconds: Long = Instant.now().epochSecond, nonce: Long = Long.MIN_VALUE
    ): MutableBlockHeader

    fun create(
        chainHash: Hash, hash: Hash, previousHash: Hash, blockParams: BlockParams,
        merkleRoot: Hash = Hash.emptyHash, seconds: Long = Instant.now().epochSecond,
        nonce: Long = Long.MIN_VALUE
    ): MutableBlockHeader

    fun create(
        blockHeader: BlockHeader
    ): MutableBlockHeader

}