package pt.um.lei.masb.blockchain.persistance.query


data class UnspecificQuery(
    override val query: String,
    override val params: Map<String, Any>
) : GenericQuery