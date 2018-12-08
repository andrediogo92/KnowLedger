package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ODocument
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Dummy data type used for the origin block.
 */
@Serializable
class DummyData : BlockChainData {


    override val approximateSize: Long = 0

    override fun store(): OElement =
        ODocument("Dummy").let {
            it.setProperty("origin", 0x01.toByte())
            it
        }

    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        BigDecimal.ZERO

    override fun toString(): String = ""
}