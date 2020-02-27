package org.knowledger.ledger.storage

import org.knowledger.ledger.data.Difficulty

interface Markable {
    fun markMined(blockheight: Long, difficulty: Difficulty)
}