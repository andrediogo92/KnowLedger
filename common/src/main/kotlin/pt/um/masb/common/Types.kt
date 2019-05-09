package pt.um.masb.common

import java.math.BigDecimal
import java.math.BigInteger

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

private val hexCode = "0123456789ABCDEF".toCharArray()

private const val TRUNC = 10

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
    printHexBinary(this)

fun Hash.truncated(): String =
    // Only convert on truncation.
    if (this.size > TRUNC) {
        printHexBinary(this.sliceArray(0..TRUNC))
    } else {
        printHexBinary(this)
    }

fun String.toHashFromHexString(): Hash =
    parseHexBinary(this)


fun Hash.toDifficulty(): Difficulty =
    BigInteger(this)


fun parseHexBinary(s: String): ByteArray {
    val len = s.length

    // "111" is not a valid hex encoding.
    if (len % 2 != 0) throw IllegalArgumentException(
        "hexBinary needs to be even-length: $s"
    )

    val out = ByteArray(len / 2)

    var i = 0
    while (i < len) {
        val h = hexToBin(s[i])
        val l = hexToBin(s[i + 1])
        if (h == -1 || l == -1) throw IllegalArgumentException(
            "contains illegal character for hexBinary: $s"
        )

        out[i / 2] = (h * 16 + l).toByte()
        i += 2
    }

    return out
}

private fun hexToBin(ch: Char): Int =
    when (ch) {
        in '0'..'9' -> ch - '0'
        in 'A'..'F' -> ch - 'A' + 10
        in 'a'..'f' -> ch - 'a' + 10
        else -> -1
    }

fun printHexBinary(data: ByteArray): String {
    val r = StringBuilder(data.size * 2)
    for (b in data) {
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return r.toString()
}
