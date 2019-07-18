package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.LedgerData

abstract class AbstractPollution(
    var unit: String,
    var city: String,
    var citySeqNum: Int
) : LedgerData {

    abstract override fun toString(): String
}
