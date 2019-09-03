package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.core.hash.Hash

interface BlockPool {
    val blocks: List<Hash>

    val firstUnconfirmed: Hash?
        get() = blocks.first()

    operator fun get(hash: Hash) =
        blocks.first {
            it == hash
        }
}