package org.knowledger.ledger.chain

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.Factories

@OptIn(ExperimentalSerializationApi::class)
data class ChainInfo(
    val hashers: Hashers,
    val encoder: BinaryFormat,
    val blockParams: BlockParams,
    val coinbaseParams: CoinbaseParams,
    val factories: Factories,
    val formula: DataFormula,
    private var difficultyTarget: Difficulty = Difficulty.INIT_DIFFICULTY,
    private var lastRecalc: Int = 0,
    //Blockheight 1 is Origin which is immediately added.
    private var blockheight: Long = 1L,
) {
    fun resetRecalculation() {
        lastRecalc = 0
    }

    fun incrementRecalculation() {
        lastRecalc++
    }

    val currentDifficulty get() = difficultyTarget

    val lastRecalculation get() = lastRecalc

    val currentBlockheight get() = blockheight
}