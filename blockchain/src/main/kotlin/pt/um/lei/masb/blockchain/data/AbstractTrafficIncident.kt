package pt.um.lei.masb.blockchain.data

abstract class AbstractTrafficIncident(
    var cityName: String,
    var citySeqNum: Int
) : BlockChainData {

    abstract override fun toString(): String
}
