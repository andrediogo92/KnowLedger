package pt.um.masb.ledger.data

import pt.um.masb.common.data.LedgerData

abstract class AbstractPollution(
    var unit: String,
    var city: String,
    var citySeqNum: Int
) : LedgerData {

    abstract override fun toString(): String
}
