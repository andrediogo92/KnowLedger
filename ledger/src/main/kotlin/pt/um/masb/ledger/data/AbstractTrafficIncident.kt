package pt.um.masb.ledger.data

import pt.um.masb.common.data.LedgerData

abstract class AbstractTrafficIncident(
    var cityName: String,
    var citySeqNum: Int
) : LedgerData {

    abstract override fun toString(): String
}
