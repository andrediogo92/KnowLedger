package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.LedgerData

abstract class AbstractTrafficIncident(
    var cityName: String,
    var citySeqNum: Int
) : LedgerData {

    abstract override fun toString(): String
}
