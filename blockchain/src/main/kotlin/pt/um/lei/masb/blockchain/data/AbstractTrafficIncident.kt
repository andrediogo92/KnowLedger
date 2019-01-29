package pt.um.lei.masb.blockchain.data

abstract class AbstractTrafficIncident(
    var trafficLat: Double,
    var trafficLon: Double,
    var date: Long,
    var cityName: String,
    var citySeqNum: Int
) : BlockChainData {

    abstract override fun toString(): String
}
