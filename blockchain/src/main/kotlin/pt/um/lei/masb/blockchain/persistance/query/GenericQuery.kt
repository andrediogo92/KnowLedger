package pt.um.lei.masb.blockchain.persistance.query

interface GenericQuery {
    val query: String
    val params: Map<String, Any>
}