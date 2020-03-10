package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.crypto.Hash

internal interface MerkleTreeUpdate {
    fun updateMerkleTree(newRoot: Hash)
}
