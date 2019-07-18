package org.knowledger.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.hashFromHexString

class HashJsonAdapter {
    @ToJson
    fun hashToJson(hash: Hash): String =
        hash.print

    @FromJson
    fun hashFromJson(hash: String): Hash =
        hash.hashFromHexString

}