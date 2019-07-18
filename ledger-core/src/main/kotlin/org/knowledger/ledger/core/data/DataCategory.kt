package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.config.LedgerConfiguration

interface DataCategory {
    val dataConstant: Long
        get() = LedgerConfiguration.DATA_BASE
}