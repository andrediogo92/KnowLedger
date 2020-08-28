package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.data.hash.toHexString

fun ByteEncodable.toHexString(): String = bytes.toHexString()

fun ByteEncodable.truncatedHexString(cutoffSize: Int = Hash.TRUNC): String =
    bytes.let { bytes ->
        if (bytes.size > cutoffSize) {
            bytes.sliceArray(0 until cutoffSize).toHexString()
        } else {
            bytes.toHexString()
        }
    }