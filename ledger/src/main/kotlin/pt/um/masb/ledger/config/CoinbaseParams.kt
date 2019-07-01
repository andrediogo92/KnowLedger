package pt.um.masb.ledger.config

import com.squareup.moshi.JsonClass
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.ledger.service.ServiceClass

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