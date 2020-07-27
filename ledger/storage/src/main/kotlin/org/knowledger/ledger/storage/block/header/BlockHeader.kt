package org.knowledger.ledger.storage.block.header

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.serial.BlockHeaderSerializationStrategy

interface BlockHeader : HashSerializable,
                        LedgerContract {
    val chainHash: Hash
    val merkleRoot: Hash
    val previousHash: Hash
    val blockParams: BlockParams
    val seconds: Long
    val nonce: Long

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(BlockHeaderSerializationStrategy, this)
}
