package org.knowledger.ledger.core.base.data

interface ByteEncodable : Comparable<ByteEncodable> {
    val bytes: ByteArray

    override fun compareTo(other: ByteEncodable): Int {
        val bytesLeft = bytes
        val bytesRight = other.bytes
        var i = 0
        var reduce = 0
        while (i < bytesLeft.size && reduce == 0) {
            reduce = bytesLeft[i].compareTo(bytesRight[i])
            i++
        }
        return reduce
    }

}