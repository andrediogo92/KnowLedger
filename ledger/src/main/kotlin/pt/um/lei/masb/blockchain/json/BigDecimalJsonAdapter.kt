package pt.um.lei.masb.blockchain.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalJsonAdapter {
    @ToJson
    fun bigDecimalToJson(bigDecimal: BigDecimal): String =
        bigDecimal.toString()

    @FromJson
    fun bigDecimalFromJson(bigInteger: String): BigDecimal =
        BigDecimal(bigInteger)
}