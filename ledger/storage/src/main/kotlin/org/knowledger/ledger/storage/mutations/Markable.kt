package org.knowledger.ledger.storage.mutations

import org.knowledger.ledger.storage.Difficulty

interface Markable {
    fun markForMining(blockheight: Long, difficulty: Difficulty)
}