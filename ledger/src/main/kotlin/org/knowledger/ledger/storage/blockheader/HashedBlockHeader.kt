package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.serial.BlockHeaderSerializer

@Serializable(with = BlockHeaderSerializer::class)
interface HashedBlockHeader : BlockHeader, Hashing {
    fun updateMerkleTree(newRoot: Hash)

    /**
     * New hash rehashes [BlockHeader] after a [BlockHeader.nonce]
     * increment.
     */
    fun newHash()

    override fun clone(): HashedBlockHeader
}