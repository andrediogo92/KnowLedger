package pt.um.lei.masb.blockchain.data

abstract class AbstractTrafficIncident(
    var trafficLat: Double,
    var trafficLon: Double,
    var date: Long,
    var cityName: String
) : BlockChainData {
    var citySeqNum: Int = 1

    abstract override fun toString(): String
}
