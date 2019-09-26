package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.core.hash.Hash

interface BlockPool {
    val blocks: List<Hash>

    val firstUnconfirmed: Hash?
        get() = blocks.firstOrNull()

    operator fun get(hash: Hash): Hash? =
        blocks.firstOrNull {
            it == hash
        }
}