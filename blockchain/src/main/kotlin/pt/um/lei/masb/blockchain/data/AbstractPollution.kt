package pt.um.lei.masb.blockchain.data

abstract class AbstractPollution(
    var lat: Double,
    var lon: Double,
    var date: Long,
    var unit: String
) : BlockChainData {
    var city: String = ""
    var citySeqNum: Int = 1

    abstract override fun toString(): String
}
