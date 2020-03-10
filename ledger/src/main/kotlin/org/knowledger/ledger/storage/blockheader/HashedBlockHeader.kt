package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.Serializable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.serial.display.BlockHeaderSerializer

@Serializable(with = BlockHeaderSerializer::class)
interface HashedBlockHeader : BlockHeader, Hashing {
    override fun clone(): HashedBlockHeader
}