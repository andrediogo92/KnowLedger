package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal

/**
 * Dummy data type used for the origin block.
 */
class DummyData : BlockChainData {

    override val approximateSize: Long = 0

    override fun digest(c: Crypter): Hash =
        c.applyHash(ByteArray(1) { 0x01 })

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Dummy")
            .apply {
                setProperty(
                    "origin",
                    0x01.toByte()
                )
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO

    override fun toString(): String = "Dummy"
}