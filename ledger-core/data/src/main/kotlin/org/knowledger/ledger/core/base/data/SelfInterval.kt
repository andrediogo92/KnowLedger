package org.knowledger.ledger.core.base.data

import java.math.BigDecimal

interface SelfInterval {
    fun calculateDiff(previous: SelfInterval): BigDecimal
}
