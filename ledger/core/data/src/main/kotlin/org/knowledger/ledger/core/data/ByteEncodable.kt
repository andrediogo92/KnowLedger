package org.knowledger.ledger.core.data

interface ByteEncodable : Comparable<ByteEncodable> {
    val bytes: ByteArray

    /**
     * Lexicographical compare of bytes.
     */
    override fun compareTo(other: ByteEncodable): Int {
        val originalBytes = bytes
        val compareBytes = other.bytes
        var compared = 0
        for (i in originalBytes.indices) {
            val unsigned = originalBytes[i].toInt() and 0xFF
            val compare = compareBytes[i].toInt() and 0xFF
            compared = unsigned.compareTo(compare)
            if (compared != 0) break
        }
        return if (compared == 0) {
            originalBytes.size.compareTo(compareBytes.size)
        } else compared
    }

}