package pt.um.masb.common.misc

import pt.um.masb.common.hash.Hash

private val hexCode = "0123456789ABCDEF".toCharArray()

val String.hashFromHexString: Hash
    get() = Hash(parseHexBinary(this))


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
    val r = CharArray(data.size * 2)
    for (j in 0 until data.size) {
        val v = data[j].toInt() and 0xFF
        r[j * 2] = hexCode[v.ushr(4)]
        r[j * 2 + 1] = hexCode[v and 0x0F]
    }
    return String(r)
}