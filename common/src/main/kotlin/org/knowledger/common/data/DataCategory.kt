package org.knowledger.common.data

import org.knowledger.common.config.LedgerConfiguration

interface DataCategory {
    val dataConstant: Long
        get() = LedgerConfiguration.DATA_BASE
}