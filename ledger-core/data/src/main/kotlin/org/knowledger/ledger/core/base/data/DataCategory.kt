package org.knowledger.ledger.core.base.data

import org.knowledger.ledger.core.base.config.DataConfiguration.DATA_BASE


interface DataCategory {
    val dataConstant: Long
        get() = DATA_BASE
}