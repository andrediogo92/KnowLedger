package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.misc.removeByUnique

data class BlockPoolImpl(
    internal val chainId: ChainId,
    internal val candidateBlocks: MutableSet<StorageID> = mutableSetOf()
) : BlockPool {
    override val blocks: Set<StorageID>
        get() = candidateBlocks

    operator fun plus(transaction: StorageID): Boolean =
        candidateBlocks.add(transaction)

    operator fun minus(transaction: StorageID): Boolean =
        candidateBlocks.removeByUnique {
            it == transaction
        }
}