package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
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