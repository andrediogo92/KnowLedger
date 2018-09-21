package pt.um.lei.masb.blockchain.data

import java.math.BigDecimal

class DummyData : BlockChainData<DummyData> {
    override fun calculateDiff(previous: DummyData): BigDecimal =
            BigDecimal.ZERO

    override fun toString(): String = ""

    override val approximateSize: Long = 0
}