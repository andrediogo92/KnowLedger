package org.knowledger.ledger.core.base.hash

private val hexCode = "0123456789ABCDEF".toCharArray()

fun String.hashFromHexString(): Hash =
    Hash(bytesFromHexString())

fun String.bytesFromHexString(): ByteArray {
    val len = length

    // "111" is not a valid hex encoding.
    require(len % 2 == 0) { "hexBinary needs to be even-length: $this" }

    val out = ByteArray(len / 2)

    var i = 0
    while (i < len) {
        val h = this[i].hexToBin()
        val l = this[i + 1].hexToBin()
        require(!(h == -1 || l == -1)) { "contains illegal character for hexBinary: $this" }

        out[i / 2] = (h * 16 + l).toByte()
        i += 2
    }

    return out
}

fun Char.hexToBin(): Int =
    when (this) {
        in '0'..'9' -> this - '0'
        in 'A'..'F' -> this - 'A' + 10
        in 'a'..'f' -> this - 'a' + 10
        else -> -1
    }

// Only convert on print.
fun ByteArray.toHexString(): String =
    String(
        CharArray(size * 2).also {
            for (j in 0 until size) {
                val v = this[j].toInt() and 0xFF
                it[j * 2] = hexCode[v.ushr(4)]
                it[j * 2 + 1] = hexCode[v and 0x0F]
            }
        }
    )