package org.knowledger.ledger.storage

import org.knowledger.ledger.crypto.Hash

interface HashUpdateable {
    fun updateHash(hash: Hash)
}