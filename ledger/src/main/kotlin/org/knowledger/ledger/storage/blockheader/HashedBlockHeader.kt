package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashing

interface HashedBlockHeader : BlockHeader, Hashing {
    fun updateMerkleTree(newRoot: Hash)

    /**
     * New hash rehashes [BlockHeader] after a [BlockHeader.nonce]
     * increment.
     */
    fun newHash()

    override fun clone(): HashedBlockHeader
}