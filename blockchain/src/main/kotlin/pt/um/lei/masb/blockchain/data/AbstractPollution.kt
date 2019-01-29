package pt.um.lei.masb.blockchain.data

abstract class AbstractPollution(
    var lat: Double,
    var lon: Double,
    var date: Long,
    var unit: String,
    var city: String,
    var citySeqNum: Int
) : BlockChainData {

    abstract override fun toString(): String
}
