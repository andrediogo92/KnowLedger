package pt.um.lei.masb.blockchain

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.experimental.or

/**
 * Hash simbolizes a **unique identifier** for
 * a data structure instance which subsumes its data
 * into an index.
 */
typealias Hash = String


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


val HEXR = 16

val MAX_DIFFICULTY: Difficulty = BigInteger(ByteArray(32) { 0xFF.toByte() }.let {
    it[0] = 0x7F.toByte()
    it
})

val INIT_DIFFICULTY: Difficulty = BigInteger(ByteArray(32).let {
    it[0] = 0x04.toByte()
    it
})

val MIN_DIFFICULTY: Difficulty = BigInteger.ZERO


fun emptyHash(): Hash = ""

fun Difficulty.print(): String = this.toString(HEXR)

fun Hash.print(): String {
    // Only convert on print.
    val hexString = StringBuilder()
    for (bHash in this.toByteArray()) {
        val hex = (0xff.toByte() or bHash).toString(HEXR)
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}

fun Hash.toDifficulty(): Difficulty = BigInteger(this.toByteArray())