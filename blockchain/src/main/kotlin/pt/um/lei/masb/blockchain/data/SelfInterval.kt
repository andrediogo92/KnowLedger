package pt.um.lei.masb.blockchain.data

import java.math.BigDecimal

interface SelfInterval {
    fun calculateDiff(previous: SelfInterval): BigDecimal
}
