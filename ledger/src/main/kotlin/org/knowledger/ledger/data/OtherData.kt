package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.common.config.LedgerConfiguration.OTHER_BASE
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.SelfInterval
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class OtherData(
    val data: java.io.Serializable
) : LedgerData {
    override fun digest(c: Hasher): Hash {
        val bao = ByteArrayOutputStream(256)
        ObjectOutputStream(bao).use {
            it.writeObject(data)
        }
        return c.applyHash(
            bao.toByteArray()
        )
    }


    override val dataConstant: Long
        get() = OTHER_BASE


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ONE

}
