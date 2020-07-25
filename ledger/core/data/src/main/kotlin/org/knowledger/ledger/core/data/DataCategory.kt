package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.data.DataConfiguration.DATA_BASE


interface DataCategory {
    val dataConstant: Long
        get() = DATA_BASE
}