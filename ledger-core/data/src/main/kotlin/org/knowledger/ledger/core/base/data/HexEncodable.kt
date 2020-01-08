package org.knowledger.ledger.core.base.data

import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.core.base.hash.toHexString

interface HexEncodable : ByteEncodable {
    fun toHexString(): String =
        bytes.toHexString()

    fun truncatedHexString(cutoffSize: Int = Hash.TRUNC): String {
        val bytes = bytes
        return if (bytes.size > cutoffSize) {
            bytes.sliceArray(0..cutoffSize).toHexString()
        } else {
            bytes.toHexString()
        }
    }
}