package pt.um.masb.common.database.query


data class UnspecificQuery(
    override val query: String,
    override val params: Map<String, Any>
) : GenericQuery