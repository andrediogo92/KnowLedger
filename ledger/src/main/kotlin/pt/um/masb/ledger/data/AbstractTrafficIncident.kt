package pt.um.masb.ledger.data

import pt.um.masb.common.data.BlockChainData

abstract class AbstractTrafficIncident(
    var cityName: String,
    var citySeqNum: Int
) : BlockChainData {

    abstract override fun toString(): String
}
