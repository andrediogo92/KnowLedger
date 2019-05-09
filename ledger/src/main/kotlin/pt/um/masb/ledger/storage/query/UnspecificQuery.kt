package pt.um.masb.ledger.storage.query


data class UnspecificQuery(
    override val query: String,
    override val params: Map<String, Any>
) : GenericQuery