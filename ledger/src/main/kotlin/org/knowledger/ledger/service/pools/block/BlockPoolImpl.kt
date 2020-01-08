package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.BlockFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader

internal data class BlockPoolImpl(
    internal val chainId: ChainId,
    private val candidateBlocks: MutableSet<Block> = mutableSetOf()
) : BlockPool {
    override val blocks: Set<Block>
        get() = candidateBlocks

    override fun refresh(hash: Hash): Outcome<BlockHeader, BlockFailure> =
        get(hash)?.newNonce()?.let {
            Outcome.Ok(it)
        } ?: Outcome.Error(BlockFailure.NoBlockForHash(hash))


    override operator fun plusAssign(block: Block) {
        candidateBlocks.add(block)
    }

    override operator fun minusAssign(block: Block) {
        candidateBlocks.remove(block)
    }
}

