package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.ChainInfo
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.results.BlockFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader

internal data class BlockPoolImpl(
    internal val chainId: ChainId,
    internal val ledgerInfo: LedgerInfo,
    internal val chainInfo: ChainInfo,
    private val candidateBlocks: MutableSet<Block> = mutableSetOf()
) : BlockPool {
    internal constructor(
        ledgerInfo: LedgerInfo,
        chainInfo: ChainInfo,
        transactionPool: TransactionPool
    ) : this(
        transactionPool.chainId,
        ledgerInfo, chainInfo
    ) {
        transactionPool.unconfirmed.chunked(ledgerInfo.ledgerParams.blockParams.blockLength).forEach {
        }
    }


    override val blocks: Set<Block>
        get() = candidateBlocks

    override fun refresh(hash: Hash): Outcome<BlockHeader, BlockFailure> =
        get(hash)?.newExtraNonce()?.let {
            Outcome.Ok(it.header)
        } ?: Outcome.Error(BlockFailure.NoBlockForHash(hash))


    override operator fun plusAssign(block: Block) {
        candidateBlocks.add(block)
    }

    override operator fun minusAssign(block: Block) {
        candidateBlocks.remove(block)
    }
}

