package pt.um.masb.common.database.query

interface GenericQuery {
    val query: String
    val params: Map<String, Any>
}