package pt.um.lei.masb.blockchain.data

import java.math.BigDecimal

interface SelfInterval<in T> {
    fun calculateDiff(previous: T): BigDecimal
}
