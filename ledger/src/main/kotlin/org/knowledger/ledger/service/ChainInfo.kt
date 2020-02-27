package org.knowledger.ledger.service

import org.knowledger.ledger.core.base.data.Difficulty

data class ChainInfo(
    private var difficultyTarget: Difficulty =
        Difficulty.INIT_DIFFICULTY,
    private var lastRecalc: Int = 0,
    //Blockheight 1 is Origin which is immediately added.
    private var blockheight: Long = 1L
) {
    fun resetRecalculation() {
        lastRecalc = 0
    }

    fun incrementRecalculation() {
        lastRecalc++
    }

    val currentDifficulty
        get() = difficultyTarget

    val lastRecalculation
        get() = lastRecalc


    val currentBlockheight
        get() = blockheight
}