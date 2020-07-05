package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.NonceRegen

internal interface MutableHashedBlockHeader : NonceRegen, HashUpdateable, HashedBlockHeader {
    /**
     * New hash rehashes [BlockHeader] after a [BlockHeader.nonce]
     * increment.
     */
    fun updateMerkleTree(newRoot: Hash)
    fun nonceReset()
}