package org.knowledger.ledger.storage

import org.knowledger.ledger.data.Difficulty

internal interface Markable {
    fun markForMining(blockheight: Long, difficulty: Difficulty)
}