package org.knowledger.ledger.core.base.data

import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.core.base.hash.toHexString

fun ByteEncodable.toHexString(): String =
    bytes.toHexString()

fun ByteEncodable.truncatedHexString(cutoffSize: Int = Hash.TRUNC): String =
    bytes.let {
        if (it.size > cutoffSize) {
            it.sliceArray(0 until cutoffSize).toHexString()
        } else {
            it.toHexString()
        }
    }