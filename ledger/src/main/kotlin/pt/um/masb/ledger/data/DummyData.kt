package pt.um.masb.ledger.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.database.NewInstanceSession
import java.math.BigDecimal

/**
 * Dummy data type used for the origin block.
 */
@JsonClass(generateAdapter = true)
class DummyData : BlockChainData {

    override val approximateSize: Long
        get() = 0

    override fun digest(c: Crypter): Hash =
        c.applyHash(ByteArray(1) { 0xCC.toByte() })

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Dummy")
            .apply {
                setProperty(
                    "origin",
                    0xCC.toByte()
                )
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO
}