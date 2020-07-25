package org.knowledger.ledger.core.data

import java.math.BigDecimal

interface SelfInterval {
    fun calculateDiff(previous: SelfInterval): BigDecimal
}
