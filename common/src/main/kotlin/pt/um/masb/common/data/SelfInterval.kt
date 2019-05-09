package pt.um.masb.common.data

import java.math.BigDecimal

interface SelfInterval {
    fun calculateDiff(previous: SelfInterval): BigDecimal
}
