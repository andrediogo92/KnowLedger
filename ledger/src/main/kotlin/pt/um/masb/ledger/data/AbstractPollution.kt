package pt.um.masb.ledger.data

import pt.um.masb.common.data.BlockChainData

abstract class AbstractPollution(
    var unit: String,
    var city: String,
    var citySeqNum: Int
) : BlockChainData {

    abstract override fun toString(): String
}
