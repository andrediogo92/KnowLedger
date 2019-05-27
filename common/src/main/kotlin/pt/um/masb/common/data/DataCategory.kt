package pt.um.masb.common.data

import pt.um.masb.common.config.LedgerConfiguration

interface DataCategory {
    val dataConstant: Long
        get() = LedgerConfiguration.DATA_BASE
}