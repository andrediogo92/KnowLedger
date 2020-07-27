package org.knowledger.ledger.storage.pools.block

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.pools.transaction.TransactionPool
import org.knowledger.ledger.storage.results.BlockFailure

internal data class BlockPoolImpl(
    internal val chainId: ChainId,
    private val candidateBlocks: MutableSet<MutableBlock> = mutableSetOf()
) : BlockPool {
    internal constructor(transactionPool: TransactionPool) : this(transactionPool.chainId)


    override val blocks: Set<MutableBlock>
        get() = candidateBlocks

    override fun refresh(hash: Hash): Outcome<MutableBlockHeader, BlockFailure> =
        get(hash)?.let {
            it.newExtraNonce()
            it.blockHeader.ok()
        } ?: BlockFailure.NoBlockForHash(hash).err()


    override operator fun plusAssign(block: MutableBlock) {
        candidateBlocks.add(block)
    }

    override operator fun minusAssign(block: MutableBlock) {
        candidateBlocks.remove(block)
    }
}

