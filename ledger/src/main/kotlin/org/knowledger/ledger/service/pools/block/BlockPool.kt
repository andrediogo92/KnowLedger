package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.BlockFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader

internal interface BlockPool : ServiceClass {
    val blocks: Set<Block>

    val firstUnconfirmed: Block?
        get() = blocks.firstOrNull()

    operator fun get(hash: Hash): Block? =
        blocks.firstOrNull {
            it.header.hash == hash
        }

    fun refresh(hash: Hash): Outcome<BlockHeader, BlockFailure>

    operator fun plusAssign(block: Block)

    operator fun minusAssign(block: Block)
}