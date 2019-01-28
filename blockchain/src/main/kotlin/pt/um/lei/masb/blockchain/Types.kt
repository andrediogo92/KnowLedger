package pt.um.lei.masb.blockchain

import mu.KotlinLogging
import java.math.BigDecimal
import java.math.BigInteger

val logger = KotlinLogging.logger {}

/**
 * Hash symbolizes a **unique identifier** for
 * a data structure instance which subsumes its data
 * into an index.
 */
typealias Hash = ByteArray


/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
typealias Payout = BigDecimal


/**
 * Difficulty is a parameter for **block committal speed**.
 * A block is mined against a difficulty, which directly
 * correlates with the speed to mining completion.
 */
typealias Difficulty = BigInteger


const val HEXR = 16

val MAX_DIFFICULTY: Difficulty = BigInteger(ByteArray(32) { 0xFF.toByte() }.let {
    it[0] = 0x7F.toByte()
    it
})

val INIT_DIFFICULTY: Difficulty = BigInteger(ByteArray(32).let {
    it[0] = 0x04.toByte()
    it
})

val MIN_DIFFICULTY: Difficulty = BigInteger.ZERO


fun emptyHash(): Hash = ByteArray(0)

fun Difficulty.print(): String = this.toString(HEXR)

fun Hash.print(): String =
// Only convert on print.
    StringBuilder().let {
        for (bHash in this) {
            val hex = String.format(
                "%02X",
                bHash
            )
            it.append(hex)
        }
        it.toString()
    }


fun Hash.truncated(): String =
// Only convert on truncation.
    StringBuilder().let {
        for (bHash in this.take(10)) {
            val hex = String.format(
                "%02X",
                bHash
            )
            it.append(hex)
        }
        it.toString()
    }


fun Hash.toDifficulty(): Difficulty = BigInteger(this)