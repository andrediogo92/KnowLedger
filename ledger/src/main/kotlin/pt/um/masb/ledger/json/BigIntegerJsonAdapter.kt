package pt.um.masb.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigInteger

class BigIntegerJsonAdapter {
    @ToJson
    fun bigIntegerToJson(bigInteger: BigInteger): String =
        bigInteger.toString()

    @FromJson
    fun bigIntegerFromJson(bigInteger: String): BigInteger =
        BigInteger(bigInteger)
}