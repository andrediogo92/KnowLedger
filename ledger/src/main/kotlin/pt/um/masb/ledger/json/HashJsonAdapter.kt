package pt.um.masb.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import pt.um.masb.common.Hash
import pt.um.masb.common.print
import pt.um.masb.common.toHashFromHexString

class HashJsonAdapter {
    @ToJson
    fun hashToJson(hash: Hash): String =
        hash.print()

    @FromJson
    fun hashFromJson(hash: String): Hash =
        hash.toHashFromHexString()

}