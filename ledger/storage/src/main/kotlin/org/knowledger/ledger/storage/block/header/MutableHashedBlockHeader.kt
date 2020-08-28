package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.storage.mutations.HashUpdateable
import org.knowledger.ledger.storage.mutations.NonceRegen
import org.knowledger.ledger.storage.mutations.RootUpdateable

interface MutableHashedBlockHeader : NonceRegen, HashUpdateable, RootUpdateable, HashedBlockHeader {
    fun nonceReset()
}