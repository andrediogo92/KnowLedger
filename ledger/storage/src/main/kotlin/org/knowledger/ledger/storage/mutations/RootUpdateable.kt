package org.knowledger.ledger.storage.mutations

import org.knowledger.ledger.crypto.Hash

interface RootUpdateable {
    fun updateMerkleRoot(merkleRoot: Hash)
}