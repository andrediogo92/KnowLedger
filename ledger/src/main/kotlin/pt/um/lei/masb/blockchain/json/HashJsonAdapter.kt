package pt.um.lei.masb.blockchain.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.ledger.toHashFromHexString

class HashJsonAdapter {
    @ToJson
    fun hashToJson(hash: Hash): String =
        hash.print()

    @FromJson
    fun hashFromJson(hash: String): Hash =
        hash.toHashFromHexString()

}