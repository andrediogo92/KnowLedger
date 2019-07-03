package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.SelfInterval
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import java.math.BigDecimal

/**
 * Dummy value type used for the origin block.
 */
@JsonClass(generateAdapter = true)
class DummyData : LedgerData {

    override val approximateSize: Long
        get() = 0

    override fun digest(c: Hasher): Hash =
        c.applyHash(ByteArray(1) { 0xCC.toByte() })


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO

    companion object {
        val DUMMY = DummyData()
    }
}