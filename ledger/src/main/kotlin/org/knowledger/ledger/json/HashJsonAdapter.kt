package org.knowledger.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.knowledger.common.hash.Hash
import org.knowledger.common.misc.hashFromHexString

class HashJsonAdapter {
    @ToJson
    fun hashToJson(hash: Hash): String =
        hash.print

    @FromJson
    fun hashFromJson(hash: String): Hash =
        hash.hashFromHexString

}