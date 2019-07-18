package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.flattenBytes
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