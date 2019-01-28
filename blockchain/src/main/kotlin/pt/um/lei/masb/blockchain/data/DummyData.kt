package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal

/**
 * Dummy data type used for the origin block.
 */
class DummyData : BlockChainData {

    override val approximateSize: Long = 0

    override fun digest(c: Crypter): Hash =
        c.applyHash("Dummy")

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Dummy")
            .let {
                it.setProperty(
                    "origin",
                    0x01.toByte()
                )
                it
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO

    override fun toString(): String = "Dummy"
}