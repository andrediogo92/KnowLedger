package pt.um.masb.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

class InstantJsonAdapter {
    @ToJson
    fun instantToJson(instant: Instant): String =
        instant.toString()

    @FromJson
    fun instantFromJson(instant: String): Instant =
        Instant.parse(instant)
}