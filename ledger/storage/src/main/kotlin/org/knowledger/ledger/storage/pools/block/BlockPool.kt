package org.knowledger.ledger.storage.pools.block

import org.knowledger.collections.SortedList
import org.knowledger.collections.searchAndGet
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableBlock

interface BlockPool : LedgerContract {
    val chainId: ChainId
    val blocks: SortedList<MutableBlock>

    val firstUnconfirmed: MutableBlock? get() = blocks.firstOrNull()

    val firstUnconfirmedNotFull: MutableBlock? get() = blocks.firstOrNull { !it.full }

    val current: MutableBlock? get() = blocks.lastOrNull()


    operator fun get(hash: Hash): MutableBlock? =
        blocks.searchAndGet(hash) { it.blockHeader.hash }
}