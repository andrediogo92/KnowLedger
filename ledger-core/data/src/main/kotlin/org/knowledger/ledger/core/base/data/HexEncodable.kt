package org.knowledger.ledger.core.base.data

import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.core.base.hash.toHexString

interface HexEncodable : ByteEncodable {
    fun toHexString(): String =
        bytes.toHexString()

    fun truncatedHexString(cutoffSize: Int = Hash.TRUNC): String =
        bytes.let {
            if (it.size > cutoffSize) {
                it.sliceArray(0 until cutoffSize).toHexString()
            } else {
                it.toHexString()
            }
        }
}