package org.knowledger.common.misc

import org.knowledger.common.hash.Hash

private val hexCode = "0123456789ABCDEF".toCharArray()

val String.hashFromHexString: Hash
    get() = Hash(parseHexBinary())


private fun String.parseHexBinary(): ByteArray {
    val len = length

    // "111" is not a valid hex encoding.
    if (len % 2 != 0) throw IllegalArgumentException(
        "hexBinary needs to be even-length: $this"
    )

    val out = ByteArray(len / 2)

    var i = 0
    while (i < len) {
        val h = this[i].hexToBin()
        val l = this[i + 1].hexToBin()
        if (h == -1 || l == -1) throw IllegalArgumentException(
            "contains illegal character for hexBinary: $this"
        )

        out[i / 2] = (h * 16 + l).toByte()
        i += 2
    }

    return out
}

private fun Char.hexToBin(): Int =
    when (this) {
        in '0'..'9' -> this - '0'
        in 'A'..'F' -> this - 'A' + 10
        in 'a'..'f' -> this - 'a' + 10
        else -> -1
    }

// Only convert on print.
val ByteArray.hexString: String
    get() = String(
        CharArray(size * 2).also {
            for (j in 0 until size) {
                val v = this[j].toInt() and 0xFF
                it[j * 2] = hexCode[v.ushr(4)]
                it[j * 2 + 1] = hexCode[v and 0x0F]
            }
        }
    )
