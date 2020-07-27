package org.knowledger.ledger.storage.pools.block

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.results.BlockFailure
import org.knowledger.ledger.storage.results.Outcome

internal interface BlockPool : LedgerContract {
    val blocks: Set<MutableBlock>

    val firstUnconfirmed: MutableBlock?
        get() = blocks.firstOrNull()

    val firstUnconfirmedNotFull: MutableBlock?
        get() = blocks.firstOrNull {
            !it.full
        }

    val current: MutableBlock?
        get() = blocks.lastOrNull()


    operator fun get(hash: Hash): MutableBlock? =
        blocks.firstOrNull {
            it.blockHeader.hash == hash
        }

    fun refresh(hash: Hash): Outcome<MutableBlockHeader, BlockFailure>

    operator fun plusAssign(block: MutableBlock)

    operator fun minusAssign(block: MutableBlock)
}