package org.knowledger.ledger.storage.block.header

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.serial.BlockHeaderSerializationStrategy

interface BlockHeader : HashSerializable,
                        LedgerContract {
    val chainId: ChainId
    val merkleRoot: Hash
    val previousHash: Hash
    val params: BlockParams
    val seconds: Long
    val nonce: Long

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(BlockHeaderSerializationStrategy, this)
}
