package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.common.misc.flattenBytes
import org.knowledger.ledger.service.ServiceClass

@JsonClass(generateAdapter = true)
data class CoinbaseParams(
    val timeIncentive: Long = 5,
    val valueIncentive: Long = 2,
    val baseIncentive: Long = 3,
    val dividingThreshold: Long = 100000
) : Hashable, ServiceClass {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                timeIncentive.bytes(),
                valueIncentive.bytes(),
                baseIncentive.bytes(),
                dividingThreshold.bytes()
            )
        )
}