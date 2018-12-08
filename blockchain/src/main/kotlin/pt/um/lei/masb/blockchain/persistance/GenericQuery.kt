package pt.um.lei.masb.blockchain.persistance

data class GenericQuery(
    val query: String,
    val params: Map<String, Any>
)